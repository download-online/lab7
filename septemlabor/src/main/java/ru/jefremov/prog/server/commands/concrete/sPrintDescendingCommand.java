package ru.jefremov.prog.server.commands.concrete;

import ru.jefremov.prog.common.commands.results.*;
import ru.jefremov.prog.common.commands.states.*;
import ru.jefremov.prog.common.network.userInfo.UserInfo;
import ru.jefremov.prog.server.commands.ServerAbstractCommand;
import ru.jefremov.prog.server.managers.ServerCommandManager;

public class sPrintDescendingCommand extends ServerAbstractCommand<CommandState, TicketsArrayResult>
{
    public sPrintDescendingCommand(String word, String description, ServerCommandManager manager) {
        super(word, description, manager);
    }

    @Override
    protected TicketsArrayResult execute(CommandState state, UserInfo info) {
        return new TicketsArrayResult(storage.printDescending());
    }
}
