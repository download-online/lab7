package ru.jefremov.prog.server.interaction;

public class Queries {
    public final static String loadQuery = """
            SELECT ticket_id, ticket_name, creationdate, price, discount, comment, ticket_type, x, y, event_id, event_name, ticketsCount, event_type\s
            FROM TICKETS JOIN EVENTS ON TICKETS.event=EVENTS.event_id;
            """;
    public final static String dropTables = """
            DROP TABLE IF EXISTS TICKETS;
            DROP TABLE IF EXISTS EVENTS;
            DROP TYPE IF EXISTS TICKETTYPE;
            DROP TYPE IF EXISTS EVENTTYPE;
            DROP SEQUENCE IF EXISTS ticketsIDseq;
            DROP SEQUENCE IF EXISTS eventsIDseq;
            """;
    public final static String createTables = """
            CREATE TYPE TICKETTYPE AS ENUM ('VIP', 'USUAL', 'CHEAP');
            CREATE TYPE EVENTTYPE AS ENUM ('FOOTBALL', 'BASKETBALL', 'THEATRE_PERFORMANCE', 'EXPOSITION');
            CREATE SEQUENCE IF NOT EXISTS ticketsIDseq START WITH 1;
            CREATE SEQUENCE IF NOT EXISTS eventsIDseq START WITH 1;
            CREATE TABLE EVENTS(
            	event_id BIGINT DEFAULT nextval('eventsIDseq') PRIMARY KEY,
            	event_name TEXT NOT NULL CHECK (length(event_name) > 0),
            	ticketsCount BIGINT NOT NULL CHECK (ticketsCount > 0),
            	event_type EVENTTYPE NOT NULL);
            CREATE TABLE IF NOT EXISTS TICKETS(
            	ticket_id INTEGER DEFAULT nextval('ticketsIDseq') PRIMARY KEY,\s
            	ticket_name TEXT NOT NULL CHECK (length(ticket_name) > 0),
            	creationdate DATE NOT NULL DEFAULT CURRENT_DATE,
            	price DOUBLE PRECISION CHECK (price > 0),
            	discount DOUBLE PRECISION CHECK (discount > 0 and discount <= 100),
            	comment TEXT NOT NULL,
            	ticket_type TICKETTYPE,
            	event BIGINT NOT NULL REFERENCES EVENTS ON DELETE CASCADE,
            	x INTEGER NOT NULL CHECK (x >- 915),
            	y DOUBLE PRECISION NOT NULL,
            	user_id INTEGER NOT NULL REFERENCES USERS ON DELETE CASCADE
            	);
            	""";

    public final static String initUsers = """
            CREATE TABLE IF NOT EXISTS USERS (
                id SERIAL PRIMARY KEY,
                login TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                salt TEXT NOT NULL
            );
            """;

    public static final String getUser = "SELECT * FROM USERS WHERE LOGIN = ?;";

    public static final String insertUser = "INSERT INTO USERS VALUES(DEFAULT, ?, ?, ?);";

    public static final String insertTicket = """
            INSERT INTO TICKETS VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;
    public static final String insertEvent = """
            INSERT INTO EVENTS VALUES (DEFAULT, ?,?,?) RETURNING event_id;""";
    public static final String updateNextTicketId = "SELECT setval('ticketsIDseq', ?, false);";
    public static final String updateNextEventId = "SELECT setval('eventsIDseq', ?, false);";
    public static final String clear = "DELETE FROM TICKETS WHERE user_id = ?;";
    public static final String selectOwned = "SELECT ticket_id from TICKETS WHERE user_id = ?;";
    public static final String remove_by_id = "DELETE FROM TICKETS WHERE ticket_id = ? and user_id = ? returning event;";
    public static final String removeEvent = "DELETE FROM EVENTS WHERE event_id = ?;";
    public static final String update = """
            UPDATE tickets
            SET ticket_name = ?, price = ?, discount = ?, comment = ?, ticket_type = ?, event = ?, x = ?, y = ?
            WHERE ticket_id = ? and user_id = ?;""";
}
