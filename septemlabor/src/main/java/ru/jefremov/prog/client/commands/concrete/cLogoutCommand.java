package ru.jefremov.prog.client.commands.concrete;

import ru.jefremov.prog.client.commands.ClientAbstractCommand;
import ru.jefremov.prog.client.managers.ClientCommandManager;
import ru.jefremov.prog.common.Printer;
import ru.jefremov.prog.common.commands.results.CommandResult;
import ru.jefremov.prog.common.commands.states.CommandState;

/**
 * Команда, обеспечивающая завершение программы без сохранения.
 */
public class cLogoutCommand extends ClientAbstractCommand<CommandState, CommandResult> {
    public cLogoutCommand(String word, ClientCommandManager manager) {
        super(word, manager, true,false);
    }

    public cLogoutCommand(String word, ClientCommandManager manager, String description) {
        super(word, manager, description, true,false);
    }

    @Override
    public CommandState formState() {
        return new CommandState();
    }

    @Override
    protected void runInterpretation(CommandResult result) {
        Printer.println("Log out");
        administrator.setUserInfo(null);
    }
}
