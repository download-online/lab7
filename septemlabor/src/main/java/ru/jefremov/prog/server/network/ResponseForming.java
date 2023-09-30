package ru.jefremov.prog.server.network;

import ru.jefremov.prog.common.Printer;
import ru.jefremov.prog.common.commands.results.CommandResult;
import ru.jefremov.prog.common.exceptions.command.CommandLaunchException;
import ru.jefremov.prog.common.network.Request;
import ru.jefremov.prog.common.network.Response;
import ru.jefremov.prog.common.network.Status;
import ru.jefremov.prog.common.serialisers.Serialisers;
import ru.jefremov.prog.server.exceptions.SerialisationException;
import ru.jefremov.prog.server.exceptions.database.UserNotFoundException;
import ru.jefremov.prog.server.managers.ServerCommandManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Objects;

public class ResponseForming {
    private final ServerCommandManager manager;
    private final Server server;

    public ResponseForming(ServerCommandManager manager, Server server) {
        this.manager = manager;
        this.server = server;
    }

    public Response formResponse(Request request, int clientId) {
        Response response;
        if (request!=null && request.userInfo==null && !Objects.equals(request.word, "register")) return new Response("Not authorized", Status.ERROR,null);
        if (request==null || request.word==null || request.state==null) {
            response = new Response("Blank request", Status.ERROR,null);
            return response;
        }

        if (request.word.equals("login")) {
            if (server.activeLogins.containsKey(clientId)) return new Response("Already logged in", Status.ERROR,null);
        }
        try {
            boolean correct = server.administrator.databaseManager.logIn(request.userInfo);
            if (correct) {
                if (request.word.equals("login")||request.word.equals("logout")) {
                    synchronized (server.activeLogins) {
                        try {
                            if (request.word.equals("login")) {
                                if (server.activeLogins.containsValue(request.userInfo.getLogin())) {
                                    response = new Response("Someone has already logged in under this nickname", Status.ERROR,null);
                                    return response;
                                } else {
                                    CommandResult result = manager.launchCommand(request.word, request.state,request.userInfo);
                                    server.activeLogins.put(clientId, request.userInfo.getLogin());
                                    response = new Response("SUCCESS", Status.OK,result);
                                    return response;
                                }
                            } else {
                                CommandResult result = manager.launchCommand(request.word, request.state,request.userInfo);
                                server.activeLogins.remove(clientId);
                                response = new Response("SUCCESS", Status.OK,result);
                                return response;
                            }

                        } catch (CommandLaunchException | ClassCastException e) {
                            response = new Response("Invalid request", Status.ERROR,null);
                        }
                        return response;
                    }
                } else if (!server.activeLogins.containsKey(clientId) || !Objects.equals(server.activeLogins.getOrDefault(clientId, null), request.userInfo.getLogin())){
                    if (!request.word.equals("register")) {
                        return new Response("Not authorized", Status.ERROR,null);
                    }
                }
            }
        } catch (UserNotFoundException e) {
            if (!request.word.equals("register") && !request.word.equals("login")) return new Response("Wrong user", Status.ERROR,null);
        }


        try {
            CommandResult result = manager.launchCommand(request.word, request.state,request.userInfo);
            response = new Response("SUCCESS", Status.OK,result);
        } catch (CommandLaunchException | ClassCastException e) {
            response = new Response("Invalid request", Status.ERROR,null);
        }

        return response;
    }

    public static void sendResponse(SocketChannel socketChannel, Response response) throws SerialisationException, IOException {
        byte[] bytes = Serialisers.responseSerialiser.serialize(response);
        int blocks_count = bytes.length/Server.BLOCK_SIZE +(bytes.length % Server.BLOCK_SIZE == 0?0:1);
        for (int i = 0; i < blocks_count; i++){
            byte[] buffer = Arrays.copyOfRange(bytes, i * Server.BLOCK_SIZE, (i + 1) * Server.BLOCK_SIZE + 1);
            buffer[buffer.length-1] = (byte)((i==blocks_count-1)?1:0);
            socketChannel.write(ByteBuffer.wrap(buffer));
            if (blocks_count>10) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
