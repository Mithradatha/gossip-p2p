package com.cse4232.gossip.helper;

import com.cse4232.gossip.helper.asn.Peer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class DataBaseHandler implements AutoCloseable {

    private static DataBaseHandler instance;

    //Readers/Writers solution
    private final Semaphore room = new Semaphore(1);
    private final Semaphore turnstile = new Semaphore(1);
    private final Semaphore leavestile = new Semaphore(1);
    private int inRoom = 0;

    public static DataBaseHandler Initialize(String connectionString) throws SQLException {
        if (instance == null) {
            instance = new DataBaseHandler(connectionString);
        }
        return instance;
    }

    public static DataBaseHandler getInstance() {
        return instance;
    }

    private Connection connection;

    private DataBaseHandler(String connectionString) throws SQLException {
        this.connection = DriverManager.getConnection(connectionString);
        Logger.getInstance().log("Successfully Connected To Database Instance");
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    public void recreate() throws SQLException {
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

        enterRoom(true);
            Statement statement = connection.createStatement();
            statement.executeUpdate(dropPeer);
            statement.executeUpdate(dropGossip);
            statement.executeUpdate(createPeer);
            statement.executeUpdate(createGossip);
            statement.close();
        leaveRoom(true);
        Logger.getInstance().log("Successfully Recreated Tables");
    }

    public void insertPeer(String name, String port, String ip) throws SQLException {
        String insertPeer = "INSERT OR IGNORE INTO Peer (Name, Port, IP) VALUES (?, ?, ?);";
        String updatePeer = "UPDATE Peer SET Port = ?, IP = ? WHERE Name = ?;";
        enterRoom(true); // Enter crit section
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
        leaveRoom(true); // Leave crit section
        Logger.getInstance().log(String.format("Successfully Upserted Peer: %s - %s:%s", name, ip, port));
    }

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
        Logger.getInstance().log(String.format("Successfully Inserted Gossip: '%s'", message));
    }

    boolean exists(String sha) throws SQLException {
        String existsGossip = "SELECT COUNT(*) AS 'Exists' FROM Gossip WHERE SHA = ?;";

        boolean result = false;
        enterRoom(false); // Enter crit as reader
            PreparedStatement preparedStatement = connection.prepareStatement(existsGossip);
            preparedStatement.setString(1, sha);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                if (resultSet.getInt("Exists") > 0) {
                    result = true;
                }
            }

            preparedStatement.close();
            resultSet.close();
        leaveRoom(false);
        return result;
    }

    public Peer[] selectPeers() throws SQLException {
        String selectPeers = "SELECT Name, Port, IP FROM Peer;";

        List<Peer> peers = new ArrayList<>();

        enterRoom(false);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectPeers);
            while (resultSet.next()) {
                String name = resultSet.getString("Name");
                int port = Integer.parseInt(resultSet.getString("Port"));
                String ip = resultSet.getString("IP");
                peers.add(new Peer(name, port, ip));
            }

            statement.close();
            resultSet.close();
        leaveRoom(false);
        Logger.getInstance().log("Successfully Selected Peers");

        return peers.toArray(new Peer[0]);
    }

    //Syncro stuff
    private void enterRoom(boolean writer)
    {
        try {
            turnstile.acquire();
                if (writer)
                    room.acquire();
                else {
                    if (inRoom == 0)
                        room.acquire();
                    inRoom ++;
                }
            turnstile.release();
        } catch (InterruptedException ignored) {}
    }

    private void leaveRoom(boolean writer)
    {
        if (writer)
            room.release();
        else
        {
            try {
                leavestile.acquire();
                    inRoom--;
                    if (inRoom == 0)
                        room.release();
                leavestile.release();
            } catch (InterruptedException ignored) {}
        }
    }


}
