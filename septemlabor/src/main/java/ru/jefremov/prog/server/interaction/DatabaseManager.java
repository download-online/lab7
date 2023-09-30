package ru.jefremov.prog.server.interaction;

import ru.jefremov.prog.common.Printer;
import ru.jefremov.prog.common.models.*;
import ru.jefremov.prog.common.network.userInfo.UserInfo;
import ru.jefremov.prog.server.exceptions.database.*;
import ru.jefremov.prog.server.managers.UserInfoSecurity;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;

public class DatabaseManager {
    private final Connection connection;
    private final String url = "jdbc:postgresql://localhost:5432/studs"; //"jdbc:postgresql://localhost/lab7?characterEncoding=ISO-8859-1";
    private final String configPath = "dbcredits.txt";
    private final String PEPPER = "e$d#WR";
    private final UserInfoSecurity security = new UserInfoSecurity();

    public DatabaseManager() throws DatabaseLaunchException {
        Properties config = new Properties();
        try {
            config.load(new FileInputStream(configPath));
        } catch (IOException e) {
            throw new DatabaseLaunchException("Config file for database connecting wasn't found");
        }
        String user = config.getProperty("user");
        String password = config.getProperty("password");

        if (user == null || password == null) {
            throw new DatabaseConfigException("Make sure that the file with the information for connecting to the database contains: username, password.");
        }

        try {
            connection = DriverManager.getConnection(url, user, password);
            initUsers();
        } catch (SQLException e) {
            throw new DatabaseLaunchException(e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }

    public LinkedHashSet<Ticket> loadCollection() throws SQLException {
        LinkedHashSet<Ticket> collection = new LinkedHashSet<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.loadQuery)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String eventTypeName = resultSet.getString("event_type");
                var event = new Event(
                        resultSet.getLong("event_id"),
                        resultSet.getString("event_name"),
                        resultSet.getLong("ticketsCount"),
                        (eventTypeName == null ? null : EventType.valueOf(eventTypeName))
                );
                var coordinates = new Coordinates(
                        resultSet.getInt("x"),
                        resultSet.getDouble("y")
                );
                String ticketTypeName = resultSet.getString("ticket_type");
                Date creationDate = resultSet.getDate("creationdate");
                var ticket = new Ticket(
                        resultSet.getInt("ticket_id"),
                        resultSet.getString("ticket_name"),
                        coordinates,
                        (creationDate == null ? null : creationDate.toLocalDate()),
                        resultSet.getDouble("price"),
                        resultSet.getDouble("discount"),
                        resultSet.getString("comment"),
                        (ticketTypeName == null ? null : TicketType.valueOf(ticketTypeName)),
                        event
                );
                collection.add(ticket);
            }
        }
        return collection;
    }

    public void recreateTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(Queries.dropTables);
        }
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(Queries.createTables);
        }
    }

    public void initUsers() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(Queries.initUsers);
        }
    }

    public boolean register(UserInfo user) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(Queries.insertUser);
        String salt = security.getSalt();
        preparedStatement.setString(1, user.getLogin());
        preparedStatement.setString(2, security.getProtected(PEPPER + user.getPassword(), salt));
        preparedStatement.setString(3, salt);
        preparedStatement.executeUpdate();
        return true;
    }

    public boolean logIn(UserInfo logging) throws UserNotFoundException {
        String username = logging.getLogin();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(Queries.getUser);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String salt = resultSet.getString("salt");
                String attempt = security.getProtected(PEPPER + logging.getPassword(), salt);
                return attempt.equals(resultSet.getString("password"));
            } else {
                throw new UserNotFoundException("Specified user doesn't exist");
            }
        } catch (SQLException e) {
            throw new UserNotFoundException("Failed to find specified user");
        }
    }

    public int getUserId(UserInfo searching) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.getUser)) {
            preparedStatement.setString(1, searching.getLogin());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int result = resultSet.getInt("id");
                return result;
            } else {
                Printer.println("nosuchid");
                return -1;
            }
        }
    }

    public boolean insertTicket(Ticket ticket, UserInfo info) throws SQLException, DatabaseException {
        int id = getUserId(info);
        var event = ticket.getEvent();
        if (id==-1) throw new DatabaseException("User doesn't exist");

        try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.updateNextEventId);
             PreparedStatement preparedStatement2 = connection.prepareStatement(Queries.updateNextTicketId);
             PreparedStatement preparedStatement3 = connection.prepareStatement(Queries.insertEvent);
             PreparedStatement preparedStatement4 = connection.prepareStatement(Queries.insertTicket)
        ) {
            preparedStatement.setLong(1, event.getId());
            preparedStatement2.setInt(1, ticket.getId());
            preparedStatement3.setString(1,event.getName());
            preparedStatement3.setLong(2,event.getTicketsCount());
            preparedStatement3.setObject(3, event.getEventType(),Types.OTHER);
            preparedStatement4.setString(1,ticket.getName());
            preparedStatement4.setDate(2,Date.valueOf(ticket.getCreationDate()));
            preparedStatement4.setDouble(3,ticket.getPrice());
            preparedStatement4.setDouble(4,ticket.getDiscount());
            preparedStatement4.setString(5, ticket.getComment());
            preparedStatement4.setObject(6,ticket.getType(),Types.OTHER);
            preparedStatement4.setLong(7, event.getId());
            preparedStatement4.setInt(8,ticket.getCoordinates().getX());
            preparedStatement4.setDouble(9,ticket.getCoordinates().getY());
            preparedStatement4.setInt(10, id);
            preparedStatement.execute();
            preparedStatement2.execute();
            preparedStatement3.execute();
            preparedStatement4.execute();
            return true;
        }
    }

    public List<Integer> getOwnedTickets(UserInfo info) throws SQLException {
        List<Integer> list = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.selectOwned)) {
            preparedStatement.setInt(1,getUserId(info));
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int ticket_id = resultSet.getInt("ticket_id");
                list.add(ticket_id);
            }
        }
        Printer.println(list);
        return list;
    }

    public void clear(List<Integer> ids, UserInfo info) throws SQLException, DatabaseException {
        int id = getUserId(info);
        if (id==-1) throw new DatabaseException("User doesn't exist");
        for (int curId:
             ids) {
            remove(curId,info);
        }
    }

    public boolean update(Ticket nt, int id, UserInfo info) throws SQLException, DatabaseException {
        int user_id = getUserId(info);
        if (user_id==-1) throw new DatabaseException("User doesn't exist");
        try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.update)) {
            preparedStatement.setString(1,nt.getName());
            preparedStatement.setDouble(2,nt.getPrice());
            preparedStatement.setDouble(3,nt.getDiscount());
            preparedStatement.setString(4,nt.getComment());
            preparedStatement.setObject(5,nt.getType(),Types.OTHER);
            try (PreparedStatement preparedStatement2 = connection.prepareStatement(Queries.updateNextEventId);
                 PreparedStatement preparedStatement3 = connection.prepareStatement(Queries.insertEvent);
            ) {
                preparedStatement2.setLong(1, nt.getEvent().getId());
                preparedStatement3.setString(1, nt.getEvent().getName());
                preparedStatement3.setLong(2, nt.getEvent().getTicketsCount());
                preparedStatement3.setObject(3, nt.getEvent().getEventType(), Types.OTHER);
                preparedStatement2.execute();
                preparedStatement3.execute();
            }
            preparedStatement.setLong(6,nt.getEvent().getId());
            preparedStatement.setInt(7,nt.getCoordinates().getX());
            preparedStatement.setDouble(8,nt.getCoordinates().getY());
            preparedStatement.setInt(9,id);
            preparedStatement.setInt(10,user_id);
            preparedStatement.execute();
            return true;
        }
    }

    public void remove(int ticket_id, UserInfo info) throws SQLException, DatabaseException {
        int id = getUserId(info);
        if (id==-1) throw new DatabaseException("User doesn't exist");
        try (PreparedStatement preparedStatement = connection.prepareStatement(Queries.remove_by_id)) {
            preparedStatement.setInt(1,ticket_id);
            preparedStatement.setInt(2,id);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                try (PreparedStatement preparedStatement2 = connection.prepareStatement(Queries.removeEvent)) {
                    preparedStatement2.setLong(1,result.getLong("event"));
                    preparedStatement2.executeUpdate();
                }
            }
        }
    }
}
