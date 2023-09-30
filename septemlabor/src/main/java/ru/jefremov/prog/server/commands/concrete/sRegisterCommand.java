package ru.jefremov.prog.server.commands.concrete;

import ru.jefremov.prog.common.Printer;
import ru.jefremov.prog.common.commands.results.IntegerResult;
import ru.jefremov.prog.common.commands.states.TicketArgumentedState;
import ru.jefremov.prog.common.commands.states.UserInfoState;
import ru.jefremov.prog.common.models.Ticket;
import ru.jefremov.prog.common.network.userInfo.UserInfo;
import ru.jefremov.prog.server.commands.ServerAbstractCommand;
import ru.jefremov.prog.server.managers.ServerCommandManager;

import java.sql.SQLException;

public class sRegisterCommand extends ServerAbstractCommand<UserInfoState, IntegerResult> {
    public sRegisterCommand(String word, String description, ServerCommandManager manager) {
        super(word, description, manager, false, true);
    }

    @Override
    protected IntegerResult execute(UserInfoState state, UserInfo info) {
        boolean success = false;
        try {
            success = administrator.databaseManager.register(state.userInfo);
        } catch (SQLException ignored) {
        }
        return new IntegerResult(success?1:0);
    }


}
