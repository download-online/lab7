package ru.jefremov.prog.server.commands.concrete;

import ru.jefremov.prog.common.commands.results.*;
import ru.jefremov.prog.common.commands.states.*;
import ru.jefremov.prog.common.network.userInfo.UserInfo;
import ru.jefremov.prog.server.commands.ServerAbstractCommand;
import ru.jefremov.prog.server.managers.ServerCommandManager;

public class sRemoveByIdCommand extends ServerAbstractCommand<IntegerArgumentedState, IntegerResult>{
    public sRemoveByIdCommand(String word, String description, ServerCommandManager manager) {
        super(word, description, manager);
    }

    @Override
    protected IntegerResult execute(IntegerArgumentedState state, UserInfo info) {
        int code = 0;
        if (!storage.hasId(state.number)) code=2;
        else code=(storage.removeById(state.number, info)?1:0);
        return new IntegerResult(code);
    }
}
