package ru.jefremov.prog.server.exceptions.database;

public class DatabaseConfigException extends DatabaseLaunchException{
    public DatabaseConfigException() {
    }

    public DatabaseConfigException(String message) {
        super(message);
    }

    public DatabaseConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseConfigException(Throwable cause) {
        super(cause);
    }

    public DatabaseConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
