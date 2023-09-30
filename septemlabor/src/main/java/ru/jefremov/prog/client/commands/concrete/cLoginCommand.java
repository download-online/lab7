package ru.jefremov.prog.client.commands.concrete;


import ru.jefremov.prog.client.commands.ClientAbstractCommand;
import ru.jefremov.prog.client.commands.arguments.ArgumentPlacement;
import ru.jefremov.prog.client.commands.arguments.primitive.StringArgument;
import ru.jefremov.prog.client.managers.ClientCommandManager;
import ru.jefremov.prog.common.Printer;
import ru.jefremov.prog.common.commands.results.IntegerResult;
import ru.jefremov.prog.common.commands.states.UserInfoState;
import ru.jefremov.prog.common.exceptions.command.CommandLaunchException;
import ru.jefremov.prog.common.network.userInfo.UserInfo;

/**
 * Команда, отвечающая за запуск скриптов из файлов.
 */
public class cLoginCommand extends ClientAbstractCommand<UserInfoState, IntegerResult> {
    private final StringArgument arg1 = new StringArgument("username", ArgumentPlacement.NEWLINE,this,"^[a-z1-8]+$");
    //private final StringArgument arg2 = new StringArgument("password", ArgumentPlacement.NEWLINE,this,"^[a-z1-8]+$");
    public cLoginCommand(String word, ClientCommandManager manager) {
        super(word, manager, true,false);
    }

    public cLoginCommand(String word, ClientCommandManager manager, String description) {
        super(word, manager, description, true,false);
    }

    @Override
    public UserInfoState formState() throws CommandLaunchException {
        if (administrator.getUserInfo()!=null) throw new CommandLaunchException("login");
        String username = arg1.getValue();
        //String password = arg2.getValue();
        Printer.print("password: ");
        String password = null;
        if (administrator.console!=null) {
            char[] symbols = administrator.console.readPassword();
            password = String.valueOf(symbols);
        }
        UserInfo info = new UserInfo(username,password);
        administrator.setUserInfo(info);
        return new UserInfoState(info);
    }

    @Override
    protected void runInterpretation(IntegerResult result) {
        if (result==null) administrator.setUserInfo(null);
        else {
            if (result.code == 0) {
                administrator.setUserInfo(null);
                Printer.println("Failed to log in with specified username and password.");
            } else if (result.code == 1) {
                administrator.setUserInfo(null);
                Printer.println("Wrong password.");
            } else Printer.println("Logged in.");
        }
    }
}
