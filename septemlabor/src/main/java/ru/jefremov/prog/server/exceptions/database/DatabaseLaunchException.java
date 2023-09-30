package ru.jefremov.prog.server.exceptions.database;

public class DatabaseLaunchException extends DatabaseException{
    public DatabaseLaunchException() {
    }

    public DatabaseLaunchException(String message) {
        super(message);
    }

    public DatabaseLaunchException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseLaunchException(Throwable cause) {
        super(cause);
    }

    public DatabaseLaunchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
