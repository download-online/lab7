package ru.jefremov.prog.common.network.userInfo;

import java.io.Serializable;

public class UserInfo implements Serializable {
    private final String login;
    private final String password;

    public UserInfo(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return login;
    }
}
