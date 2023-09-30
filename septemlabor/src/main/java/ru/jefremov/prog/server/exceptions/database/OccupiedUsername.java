package ru.jefremov.prog.server.exceptions.database;

public class OccupiedUsername extends DatabaseException{
    public OccupiedUsername() {
    }

    public OccupiedUsername(String message) {
        super(message);
    }

    public OccupiedUsername(String message, Throwable cause) {
        super(message, cause);
    }

    public OccupiedUsername(Throwable cause) {
        super(cause);
    }

    public OccupiedUsername(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
