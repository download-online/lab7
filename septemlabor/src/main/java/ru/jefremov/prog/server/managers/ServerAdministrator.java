package ru.jefremov.prog.server.managers;

import ru.jefremov.prog.common.Printer;
import ru.jefremov.prog.common.models.validation.CoordinatesValidator;
import ru.jefremov.prog.common.models.validation.EventValidator;
import ru.jefremov.prog.common.models.validation.TicketValidator;
import ru.jefremov.prog.server.exceptions.SavedCollectionInteractionException;
import ru.jefremov.prog.server.interaction.DatabaseManager;

public class ServerAdministrator {
    public final DatabaseManager databaseManager;
    /**
     * Валидатор событий
     */
    public final EventValidator eventValidator = new EventValidator();
    /**
     * Валидатор координат
     */
    public final CoordinatesValidator coordinatesValidator = new CoordinatesValidator();
    /**
     * Валидатор билетов
     */
    public final TicketValidator ticketValidator = new TicketValidator(eventValidator,coordinatesValidator);
    /**
     * Хранилище
     */
    public final Storage storage = new Storage(this, ticketValidator);

    /**
     * Менеджер команд
     */
    public final ServerCommandManager commandManager;

    /**
     * Конструктор для администратора
     */
    public ServerAdministrator(DatabaseManager manager) {
        commandManager = new ServerCommandManager(storage);
        databaseManager = manager;
    }

    public void save() {
        try {
            commandManager.launchCommand("save", ServerCommandManager.blankState,null);
            Printer.println("Collection saved.");
        } catch (Exception e) {
            Printer.println("Saving failed");
        }
    }

    public void executeSaving() throws SavedCollectionInteractionException {
        Printer.println("SAVED!");
    }

    public void exit() {
        save();

    }
}
