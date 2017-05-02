package edu.cse4232.gossip.context;

import edu.cse4232.gossip.asn1.Peer;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.Date;
import java.util.concurrent.*;

/**
 * Handles Server Database Connection
 */
public class DataBaseHandler implements AutoCloseable {

    private static final String APPLICATION_DATETIME_FORMAT = "yyyyMMddHHmmss";

    //private static DataBaseHandler instance;

    //Readers/Writers solution
    private final Semaphore room = new Semaphore(1);
    private final Semaphore turnstile = new Semaphore(1);
    private final Semaphore leavestile = new Semaphore(1);
    private int inRoom = 0;

    private Connection connection;
    private ScheduledExecutorService executor;

    public DataBaseHandler(String connectionString, int delay) throws SQLException {
        this.connection = DriverManager.getConnection(connectionString);
        this.executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> removeForgottenPeers(delay), delay, delay, TimeUnit.SECONDS);
        //Logger.getInstance().log("Successfully Connected To Database Instance");
    }

    public void updatePeerLastSeen(String ip, int port) {

        String lastSeen = LocalDateTime.now().format(DateTimeFormatter.ofPattern(APPLICATION_DATETIME_FORMAT));

        System.err.println(ip + ":" + port + " last seen at " + lastSeen);

        String updatePeer = "UPDATE Peer SET LastSeen = ? WHERE IP = ? AND Port = ?;";

        enterRoom(true);

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(updatePeer);
            preparedStatement.setString(1, lastSeen);
            preparedStatement.setString(2, ip);
            preparedStatement.setInt(3, port);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException ignored) {}

        leaveRoom(true);
    }

    private void removeForgottenPeers(int delay) {
        System.err.println("CHECKING");

/*        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate currentDate = LocalDate.now();
        java.sql.Date expirationDate = java.sql.Date.valueOf(currentDate.minus(2, ChronoUnit.DAYS).format(formatter));*/

        String expirationDate = LocalDateTime.now().minus(delay, ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ofPattern(APPLICATION_DATETIME_FORMAT));

        System.err.println(expirationDate);

        String selectPeers = "SELECT Name, LastSeen FROM PEER;";
        String deletePeers = "DELETE FROM Peer WHERE LastSeen <= ?;";


        enterRoom(true);
        try {

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectPeers);
            while (resultSet.next()) {
                String name = resultSet.getString("Name");
                String lastSeen = resultSet.getString("LastSeen");
                System.err.println(name + " " + lastSeen);
            }

            statement.close();
            resultSet.close();


            PreparedStatement preparedStatement = connection.prepareStatement(deletePeers);
            preparedStatement.setString(1, expirationDate);
            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (SQLException ignored) {}
        leaveRoom(true);
    }

    /**
     * Closes Database Connection and Peer Timer
     *
     * @throws SQLException Connection.close()
     */
    @Override
    public void close() throws SQLException {
        connection.close();
    }


    /**
     * Drops All Tables
     * Creates All Tables
     *
     * @throws SQLException Statement.executeUpdate()
     */
    public void recreate() throws SQLException {
        String dropPeer = "DROP TABLE IF EXISTS Peer;";
        String dropGossip = "DROP TABLE IF EXISTS Gossip;";

        String createPeer = "CREATE TABLE Peer " +
                "(Name TEXT PRIMARY KEY NOT NULL, " +
                "Port INTEGER NOT NULL, " +
                "IP TEXT NOT NULL, " +
                "LastSeen TEXT NOT NULL);";

        String createGossip = "CREATE TABLE Gossip " +
                "(SHA TEXT PRIMARY KEY NOT NULL, " +
                "DT TEXT NOT NULL, " +
                "Message TEXT NOT NULL);";

        enterRoom(true);
        Statement statement = connection.createStatement();
        statement.executeUpdate(dropPeer);
        statement.executeUpdate(dropGossip);
        statement.executeUpdate(createPeer);
        statement.executeUpdate(createGossip);
        statement.close();
        leaveRoom(true);
        //Logger.getInstance().log("Successfully Recreated Tables");
    }

    /**
     * @param name
     * @param port
     * @param ip
     * @throws SQLException PreparedStatement.executeUpdate()
     */
    public void insertPeer(String name, int port, String ip) throws SQLException {
        String insertPeer = "INSERT OR IGNORE INTO Peer (Name, Port, IP, LastSeen) VALUES (?, ?, ?, ?);";
        String updatePeer = "UPDATE Peer SET Port = ?, IP = ? WHERE Name = ?;";

        //java.sql.Date date = new java.sql.Date(new Date().getTime());

        /*DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate currentDate = LocalDate.now();
        java.sql.Date expirationDate = java.sql.Date.valueOf(currentDate.minus(3, ChronoUnit.DAYS).format(formatter));*/

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern(APPLICATION_DATETIME_FORMAT));

        System.err.println("Inserting Peer at " + ip + ":" + port + " last seen " + date);

        enterRoom(true); // Enter crit section
        PreparedStatement preparedStatement = connection.prepareStatement(insertPeer);
        preparedStatement.setString(1, name);
        preparedStatement.setInt(2, port);
        preparedStatement.setString(3, ip);
        preparedStatement.setString(4, date);
        preparedStatement.executeUpdate();
        preparedStatement.close();

        preparedStatement = connection.prepareStatement(updatePeer);
        preparedStatement.setInt(1, port);
        preparedStatement.setString(2, ip);
        preparedStatement.setString(3, name);
        preparedStatement.executeUpdate();
        preparedStatement.close();
        leaveRoom(true); // Leave crit section
        //Logger.getInstance().log(String.format("Successfully Upserted Peer: %s - %s:%s", name, ip, port));
    }

    /**
     * @param sha
     * @param dt
     * @param message
     * @throws SQLException PreparedStatement.executeUpdate()
     */
    public void insertGossip(String sha, String dt, String message) throws SQLException {
        String insertGossip = "INSERT INTO Gossip (SHA, DT, Message) VALUES (?, ?, ?);";

        enterRoom(true); // Enter protected section as writer
        PreparedStatement preparedStatement = connection.prepareStatement(insertGossip);
        preparedStatement.setString(1, sha);
        preparedStatement.setString(2, dt);
        preparedStatement.setString(3, message);
        preparedStatement.executeUpdate();
        preparedStatement.close();
        leaveRoom(true); // Leave protected section as writer
        //Logger.getInstance().log(String.format("Successfully Inserted Gossip: '%s'", message));
    }

    /**
     * Tests for Duplicate Gossip Message
     *
     * @param sha
     * @return True if SHA-256 Hash already exists
     * @throws SQLException PreparedStatement.executeQuery()
     */
    public boolean exists(String sha) throws SQLException {
        String existsGossip = "SELECT COUNT(*) AS 'Exists' FROM Gossip WHERE SHA = ?;";

        boolean result = false;
        enterRoom(false); // Enter crit as reader
        PreparedStatement preparedStatement = connection.prepareStatement(existsGossip);
        preparedStatement.setString(1, sha);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            int occurances = resultSet.getInt("Exists");
            if (occurances > 0) {
                result = true;
                //Logger.getInstance().log("Duplicate Gossp: " + sha);
            }
        }

        preparedStatement.close();
        resultSet.close();
        leaveRoom(false);
        return result;
    }

    /**
     * Deletes Peer on LEAVE Message
     *
     * @param name
     * @throws SQLException PreparedStatement.executeUpdate()
     */
    public void removeUser(String name) throws SQLException {
        String removePeer = "DELETE FROM Peer WHERE Name=?;";

        enterRoom(true);
        PreparedStatement preparedStatement = connection.prepareStatement(removePeer);
        preparedStatement.setString(1, name);
        preparedStatement.executeUpdate();
        preparedStatement.close();
        leaveRoom(true);
        //Logger.getInstance().log(String.format("Successfully deleted user %s", name));
    }

    /**
     * @return Sequence of Peers Known to Server
     * @throws SQLException Statement.executeQuery()
     */
    public Peer[] selectPeers() throws SQLException {
        String selectPeers = "SELECT Name, Port, IP FROM Peer;";

        List<Peer> peers = new ArrayList<>();

        enterRoom(false);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(selectPeers);
        while (resultSet.next()) {
            String name = resultSet.getString("Name");
            int port = resultSet.getInt("Port");
            String ip = resultSet.getString("IP");
            peers.add(new Peer(name, port, ip));
        }

        statement.close();
        resultSet.close();
        leaveRoom(false);
        //Logger.getInstance().log("Successfully Selected Peers");

        return peers.toArray(new Peer[0]);
    }

    /**
     * First Writer Locks Room
     * First Reader Locks Writers
     *
     * @param writer
     */
    private void enterRoom(boolean writer) {
        try {
            turnstile.acquire();
            if (writer)
                room.acquire();
            else {
                if (inRoom == 0)
                    room.acquire();
                inRoom++;
            }
            turnstile.release();
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Writer Unlocks Room
     * Last Reader Unlocks Room
     *
     * @param writer
     */
    private void leaveRoom(boolean writer) {
        if (writer)
            room.release();
        else {
            try {
                leavestile.acquire();
                inRoom--;
                if (inRoom == 0)
                    room.release();
                leavestile.release();
            } catch (InterruptedException ignored) {
            }
        }
    }


}
