package ru.jefremov.prog.server.commands;

import ru.jefremov.prog.common.commands.AbstractCommand;
import ru.jefremov.prog.common.commands.results.CommandResult;
import ru.jefremov.prog.common.commands.states.CommandState;
import ru.jefremov.prog.common.exceptions.command.CommandLaunchException;
import ru.jefremov.prog.common.network.userInfo.UserInfo;
import ru.jefremov.prog.server.managers.ServerAdministrator;
import ru.jefremov.prog.server.managers.ServerCommandManager;
import ru.jefremov.prog.server.managers.Storage;

public abstract class ServerAbstractCommand<T extends CommandState, U extends CommandResult> extends AbstractCommand<T, U> {
    public final ServerAdministrator administrator;
    public final ServerCommandManager manager;
    public final Storage storage;
    public final boolean serverOnly;
    public final boolean systemic;

    public ServerAbstractCommand(String word, String description, ServerCommandManager manager, boolean serverOnly, boolean systemic) {
        super(word, description);
        this.systemic = systemic;
        if (manager==null) throw new IllegalArgumentException("Command manager must not be null");
        manager.addCommand(this);
        this.manager = manager;
        this.administrator = manager.administrator;
        this.storage = manager.getStorage();
        this.serverOnly = serverOnly;
    }
    public ServerAbstractCommand(String word, String description, ServerCommandManager manager) {
        this(word,description,manager,false, false);
    }

    @Override
    public final U launch(CommandState state, UserInfo info) throws CommandLaunchException {
        try {
            T castedState = (T) state;
            return execute(castedState, info);
        } catch (ClassCastException e) {
            throw new CommandLaunchException("Attempted to launch command from wrong state");
        }
    }

    protected abstract U execute(T state, UserInfo info);
}
