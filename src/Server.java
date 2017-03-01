
import javax.xml.crypto.Data;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.List;

public class Server {

    private static final String nullOp = "%";
    private static final String inSep = ":";
    private static final String outSep = "|";
    private static final String kv = "=";

    public static void main(String[] args) {

        //Class.forName("org.sqlite.JDBC");

        //int server_port = Integer.parseInt(args[0]);
        //String server_db = args[1];

        int serverPort = 2345;
        String dbConnectionString = "jdbc:sqlite:test.db";


        try (DataBaseHandler db = DataBaseHandler.getInstance(dbConnectionString);) {

            db.recreate();

            // TCP Server
            try (ServerSocket serverSocket = new ServerSocket(serverPort);) {
                while (true) {
                    try (
                            Socket clientSocket = serverSocket.accept();
                            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    ) {
                        out.println("HELLO");
                        String request;
                        while ((request = in.readLine()) != null) {
                            String[] input = request.split(inSep);
                            String protocol = input[0];
                            System.out.println(protocol);
                            switch (protocol) {
                                case "GOSSIP": {
                                    String sha = input[1];
                                    String dt = input[2];
                                    String message = input[3].substring(0, input[3].indexOf(nullOp));
                                    if (db.exists(sha)) {
                                        out.println("DISCARDED");
                                        out.flush();
                                    } else {
                                        System.out.println(sha);
                                        System.out.println(dt);
                                        System.out.println(message);
                                        db.insertGossip(sha, dt, message);
                                    }
                                    break;
                                }
                                case "PEER": {
                                    String name = input[1];
                                    String port = input[2].split(kv)[1];
                                    String ip = input[3].substring(0, input[3].indexOf(nullOp)).split(kv)[1];
                                    db.insertPeer(name, port, ip);
                                    break;
                                }
                                case "PEERS?": {
                                    List<String[]> peers =  db.selectPeers();
                                    StringBuilder peerResponse = new StringBuilder();
                                    int count = peers.size();

                                    peerResponse.append(String.format("PEERS|%s|", count));

                                    for (String[] peer : peers) {

                                        String name = peer[0];
                                        String port = peer[1];
                                        String ip = peer[2];

                                        peerResponse.append(String.format("%s:PORT=%s:IP=%s|", name, port, ip));
                                    }

                                    peerResponse.append(nullOp);
                                    out.println(peerResponse);
                                    break;
                                }
                                default:
                                    out.println("Unknown Command: " + protocol);
                                    break;
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        } catch (SQLException exp) {
            exp.printStackTrace();
        }
    }
}
