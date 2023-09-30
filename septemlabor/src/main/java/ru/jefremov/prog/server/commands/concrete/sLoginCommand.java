package ru.jefremov.prog.server.commands.concrete;

import ru.jefremov.prog.common.Printer;
import ru.jefremov.prog.common.commands.results.IntegerResult;
import ru.jefremov.prog.common.commands.states.UserInfoState;
import ru.jefremov.prog.common.network.userInfo.UserInfo;
import ru.jefremov.prog.server.commands.ServerAbstractCommand;
import ru.jefremov.prog.server.exceptions.database.UserNotFoundException;
import ru.jefremov.prog.server.managers.ServerCommandManager;

public class sLoginCommand extends ServerAbstractCommand<UserInfoState, IntegerResult> {
    public sLoginCommand(String word, String description, ServerCommandManager manager) {
        super(word, description, manager, false, true);
    }

    @Override
    protected IntegerResult execute(UserInfoState state, UserInfo info) {
        int code = 0;
        try {
            code = (administrator.databaseManager.logIn(state.userInfo) ?2:1);
        } catch (UserNotFoundException ignored) {}
        return new IntegerResult(code);
    }


}
