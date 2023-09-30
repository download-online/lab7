package ru.jefremov.prog.server.commands.concrete;

import ru.jefremov.prog.common.Printer;
import ru.jefremov.prog.common.commands.results.*;
import ru.jefremov.prog.common.commands.states.*;
import ru.jefremov.prog.common.network.userInfo.UserInfo;
import ru.jefremov.prog.server.commands.ServerAbstractCommand;
import ru.jefremov.prog.server.exceptions.SavedCollectionInteractionException;
import ru.jefremov.prog.server.managers.ServerCommandManager;

public class sSaveCommand extends ServerAbstractCommand<CommandState, CommandResult>{
    public sSaveCommand(String word, String description, ServerCommandManager manager) {
        super(word, description, manager, true, true);
    }

    @Override
    protected CommandResult execute(CommandState state, UserInfo info) {
        try {
            administrator.executeSaving();
        } catch (SavedCollectionInteractionException e) {
            Printer.println(e.getMessage());
            Printer.println("Collection saving failed");
        }
        return new CommandResult();
    }
}
