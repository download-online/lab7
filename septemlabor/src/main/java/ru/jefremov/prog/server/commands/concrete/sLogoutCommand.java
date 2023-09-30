package ru.jefremov.prog.server.commands.concrete;

import ru.jefremov.prog.common.Printer;
import ru.jefremov.prog.common.commands.results.CommandResult;
import ru.jefremov.prog.common.commands.results.IntegerResult;
import ru.jefremov.prog.common.commands.states.CommandState;
import ru.jefremov.prog.common.commands.states.UserInfoState;
import ru.jefremov.prog.common.network.userInfo.UserInfo;
import ru.jefremov.prog.server.commands.ServerAbstractCommand;
import ru.jefremov.prog.server.managers.ServerCommandManager;

public class sLogoutCommand extends ServerAbstractCommand<CommandState, CommandResult> {
    public sLogoutCommand(String word, String description, ServerCommandManager manager) {
        super(word, description, manager, false, true);
    }

    @Override
    protected CommandResult execute(CommandState state, UserInfo info) {
        return new CommandResult();
    }


}
