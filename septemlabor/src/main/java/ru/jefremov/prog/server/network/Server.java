package ru.jefremov.prog.server.network;

import ru.jefremov.prog.common.Printer;
import ru.jefremov.prog.common.exceptions.ExitInterruptionException;
import ru.jefremov.prog.common.exceptions.command.CommandLaunchException;
import ru.jefremov.prog.common.network.Request;
import ru.jefremov.prog.common.network.Response;
import ru.jefremov.prog.server.commands.ServerAbstractCommand;
import ru.jefremov.prog.server.commands.concrete.sSaveCommand;
import ru.jefremov.prog.server.exceptions.*;
import ru.jefremov.prog.server.managers.ServerAdministrator;
import ru.jefremov.prog.server.managers.ServerCommandManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    public static final int BLOCK_SIZE = 10000;
    private final Selector selector;
    private final ServerSocketChannel server;
    private final Scanner interactiveReader;
    private SocketAddress address;
    private boolean running;
    private Request request;
    private Response response;

    private final AtomicInteger clientIdentifier = new AtomicInteger(1);
    protected final Map<Integer, String> activeLogins = Collections.synchronizedMap(new HashMap<>());
    public final ServerAdministrator administrator;
    public final ServerCommandManager manager;
    private final ResponseForming responseForming;
    private static final ExecutorService formingPool = Executors.newFixedThreadPool(5);
    private static final ExecutorService sendingPool = Executors.newCachedThreadPool();
    public Server(int port, ServerAdministrator administrator, Scanner reader) throws ServerLaunchException {
        Runtime.getRuntime().addShutdownHook(new Thread(administrator::exit));
        this.interactiveReader = reader;
        this.administrator = administrator;
        this.manager = administrator.commandManager;
        responseForming = new ResponseForming(manager,this);
        try {
            selector = Selector.open();
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);
        } catch (ClosedChannelException e) {
            throw new ServerLaunchException("Channel closed");
        } catch (IOException e) {
            throw new ServerLaunchException("Server launch problem: "+e.getMessage());
        }

        try {
            address = new InetSocketAddress(port);
            server.bind(address);
        } catch (IOException e) {
            throw new ServerLaunchException("Occupied port");
        }
        running = true;

        new Thread(() -> {
            while (interactiveReader.hasNextLine()) {
                String userInput = interactiveReader.nextLine();
                if (userInput==null) continue;
                userInput = userInput.strip();
                ServerAbstractCommand<?,?> command = manager.getCommand(userInput);
                if (!(command instanceof sSaveCommand)) {
                    Printer.println("You can only launch \"save\" command here.");
                } else {
                    try {
                        manager.launchCommand(userInput, ServerCommandManager.blankState, null);
                    } catch (CommandLaunchException e) {
                        Printer.println("Failed to run command from server console.");
                    }
                }
            }
            interactiveReader.close();
        }).start();
    }

    public void run() {
        try {
            while (running) {
                int selected = selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                for (var iter = keys.iterator(); iter.hasNext(); ) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    if (key.isValid()) {
                        if (key.isAcceptable()) {
                            doAccept(key);
                        }
                    } else key.cancel();
                }
            }
        } catch (IOException e) {
            Printer.println("Client disconnected");
        } catch (ClientAcceptingException e) {
            Printer.println(e.getMessage());
        } catch (Exception e) {
            Printer.println(e.getMessage());
            stopServer();
        }
        try {
            selector.close();
        } catch (IOException e) {
            Printer.println("Failed to close selector correctly.");
        }
    }

    private void doAccept(SelectionKey key) throws ClientAcceptingException {
        var ssc = (ServerSocketChannel) key.channel();
        try {
            SocketChannel sc;
            synchronized (server) {
                sc = ssc.accept();
            }
            new Thread(()->{
                int id = clientIdentifier.getAndIncrement();
                Printer.println("Client #"+id+" connected");
                try {
                    while (true) {
                        Request request = RequestReader.readRequest(sc);
                        formingPool.submit(()->{
                            Response response = responseForming.formResponse(request, id);
                            sendingPool.submit(()->{
                                try {
                                    ResponseForming.sendResponse(sc, response);
                                } catch (SerialisationException | IOException e) {
                                    Printer.println("Failed to send response");
                                    stopServer();
                                }
                            });
                        });
                    }
                } catch (IOException | SerialisationException | ClassCastException e) {
                    Printer.println("Client disconnected");
                }
                activeLogins.remove(id);
            }).start();
        } catch (IOException e) {
            throw new ClientAcceptingException("Failed to accept client: "+e.getMessage());
        }
    }

    public void stopServer() {
        running = false;
        administrator.save();
        try {
            server.close();
        } catch (IOException e) {
            Printer.println("Failed to close server correctly");
        }
        System.exit(0);
    }
}
