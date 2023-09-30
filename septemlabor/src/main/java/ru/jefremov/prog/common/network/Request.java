package ru.jefremov.prog.common.network;

import ru.jefremov.prog.common.commands.states.CommandState;
import ru.jefremov.prog.common.network.userInfo.UserInfo;

import java.io.Serializable;

public class Request implements Serializable {
    public final String word;
    public final CommandState state;
    public final UserInfo userInfo;
    public final boolean requiresLargeArrays;

    public Request(String word, CommandState state, UserInfo userInfo, boolean requiresLargeArrays) {
        this.word = word;
        this.state = state;
        this.userInfo = userInfo;
        this.requiresLargeArrays = requiresLargeArrays;
    }
}
