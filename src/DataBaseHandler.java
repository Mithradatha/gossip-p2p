import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseHandler implements AutoCloseable {

    private static DataBaseHandler instance;

    static DataBaseHandler getInstance(String connectionString) throws SQLException {
        if (instance == null) {
            instance = new DataBaseHandler(connectionString);
        }
        return instance;
    }

    private Connection connection;

    private DataBaseHandler(String connectionString) throws SQLException {
        this.connection = DriverManager.getConnection(connectionString);
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    void recreate() throws SQLException {
        String dropPeer = "DROP TABLE IF EXISTS Peer;";
        String dropGossip = "DROP TABLE IF EXISTS Gossip;";

        String createPeer = "CREATE TABLE Peer " +
                "(Name TEXT PRIMARY KEY NOT NULL, " +
                "Port TEXT NOT NULL, " +
                "IP TEXT NOT NULL);";

        String createGossip = "CREATE TABLE Gossip " +
                "(SHA TEXT PRIMARY KEY NOT NULL, " +
                "DT TEXT NOT NULL, " +
                "Message TEXT NOT NULL);";

        Statement statement = connection.createStatement();
        statement.executeUpdate(dropPeer);
        statement.executeUpdate(dropGossip);
        statement.executeUpdate(createPeer);
        statement.executeUpdate(createGossip);
        statement.close();
    }

    void insertPeer(String name, String port, String ip) throws SQLException {
        String insertPeer = "INSERT OR IGNORE INTO Peer (Name, Port, IP) VALUES (?, ?, ?);";
        String updatePeer = "UPDATE Peer SET Port = ?, IP = ? WHERE Name = ?;";

        PreparedStatement preparedStatement = connection.prepareStatement(insertPeer);
        preparedStatement.setString(1, name);
        preparedStatement.setString(2, port);
        preparedStatement.setString(3, ip);
        preparedStatement.executeUpdate();
        preparedStatement.close();

        preparedStatement = connection.prepareStatement(updatePeer);
        preparedStatement.setString(1, port);
        preparedStatement.setString(2, ip);
        preparedStatement.setString(3, name);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    void insertGossip(String sha, String dt, String message) throws SQLException {
        String insertGossip = "INSERT INTO Gossip (SHA, DT, Message) VALUES (?, ?, ?);";

        PreparedStatement preparedStatement = connection.prepareStatement(insertGossip);
        preparedStatement.setString(1, sha);
        preparedStatement.setString(2, dt);
        preparedStatement.setString(3, message);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    boolean exists(String sha) throws SQLException {
        String existsGossip = "SELECT COUNT(*) AS 'Exists' FROM Gossip WHERE SHA = ?;";

        boolean result = false;

        PreparedStatement preparedStatement = connection.prepareStatement(existsGossip);
        preparedStatement.setString(1, sha);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            if (resultSet.getInt("Exists") > 0) { result = true; }
        }

        preparedStatement.close();
        resultSet.close();

        return result;
    }

    List<String[]> selectPeers() throws SQLException {
        String selectPeers = "SELECT Name, Port, IP FROM Peer;";

        List<String[]> result = new ArrayList<>();

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(selectPeers);
        while (resultSet.next()) {
            String name = resultSet.getString("Name");
            String port = resultSet.getString("Port");
            String ip = resultSet.getString("IP");
            String[] peer = new String[3];
            peer[0] = name;
            peer[1]= port;
            peer[2] = ip;
            result.add(peer);
        }

        statement.close();
        resultSet.close();

        return result;
    }
}
