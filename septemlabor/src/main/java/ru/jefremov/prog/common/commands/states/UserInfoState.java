package ru.jefremov.prog.common.commands.states;


import ru.jefremov.prog.common.network.userInfo.UserInfo;

import java.io.Serializable;

public class UserInfoState extends CommandState implements Serializable {
    public final UserInfo userInfo;

    public UserInfoState(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
