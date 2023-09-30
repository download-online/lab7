package ru.jefremov.prog.server;

import ru.jefremov.prog.common.Printer;
import ru.jefremov.prog.common.models.Ticket;
import ru.jefremov.prog.common.network.userInfo.UserInfo;
import ru.jefremov.prog.server.exceptions.SavedCollectionInteractionException;
import ru.jefremov.prog.server.exceptions.ServerLaunchException;
import ru.jefremov.prog.server.exceptions.database.DatabaseLaunchException;
import ru.jefremov.prog.server.interaction.DatabaseManager;
import ru.jefremov.prog.server.managers.ServerAdministrator;
import ru.jefremov.prog.server.network.Server;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Scanner;

public class MainServer {
    public static int port = 6086;
    public static Server server;

    public static void main(String[] args) {
        Printer.println("Server "+port);
        String exitMessage = "Program execution finished";

        Scanner serverInteraction = new Scanner(System.in);

        DatabaseManager databaseManager;
        try {
            databaseManager = new DatabaseManager();
            Printer.println("Connected to database");

        } catch (DatabaseLaunchException e) {
            Printer.println("Failed to connect to the database.");
            Printer.println(exitMessage);
            return;
        }



        ServerAdministrator administrator = new ServerAdministrator(databaseManager);

        boolean loaded = false;
        try {
            LinkedHashSet<Ticket> loadedCollection = databaseManager.loadCollection();
            loaded = administrator.storage.loadCollection(loadedCollection);
            if (!loaded) Printer.println(administrator.storage.reviewCollection(loadedCollection));
        } catch (SQLException lazy) {
        }

        if (!loaded) {
            boolean recreated = false;
            Printer.println("Failed to load collection.");
            Printer.println("Type \"yes\" if you want to clear database and recreate tables:");
            Printer.print("> ");
            if (serverInteraction.hasNext()) {
                String userInput = serverInteraction.nextLine();
                if (Objects.equals(userInput, "yes")) {
                    try {
                        databaseManager.recreateTables();
                        loaded = administrator.storage.loadCollection(databaseManager.loadCollection());
                        recreated = true;
                    } catch (SQLException e) {
                        Printer.println(e.getMessage());
                        Printer.println("Failed to recreate tables.");
                    }
                }
            }
            if (!recreated || !loaded) {
                Printer.println("Recreation cancelled");
                Printer.println(exitMessage);
                return;
            }
        }
        Printer.println("Collection was successfully loaded.");


        try {
            Printer.println("Server launched");
            server = new Server(port,administrator, serverInteraction);
            server.run();
            administrator.save();
        } catch (ServerLaunchException e) {
            Printer.println("Failed to run server");
        } catch (Exception e) {
            Printer.println("Unexpected error");
        }
        administrator.save();
        databaseManager.closeConnection();
    }
}