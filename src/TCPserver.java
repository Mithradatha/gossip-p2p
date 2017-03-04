import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

class TCPserver implements Runnable {

    private int port;
    private DataBaseHandler db;

    TCPserver(int port, DataBaseHandler db) {
        this.port = port;
        this.db = db;
    }

    @Override
    public void run() {
        // TCP Server
        try (ServerSocket serverSocket = new ServerSocket(port);) {
            while (true) {
                try (
                        Socket clientSocket = serverSocket.accept();
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                ) {
                    // Greetings
                    out.println("HELLO");

                    String request;
                    while ((request = in.readLine()) != null) {

                        String[] input = request.split(Parser.inSep);
                        String command = input[0];
                        System.out.println(command);

                        switch (command) {
                            case "GOSSIP": {
                                String sha = input[1];
                                String dt = input[2];
                                String message = input[3].substring(0, input[3].indexOf(Parser.nullOp));
                                if (db.exists(sha)) {
                                    out.println("DISCARDED");
                                    out.flush();
                                } else {
                                    System.out.println(sha);
                                    System.out.println(dt);
                                    System.out.println(message);
                                    db.insertGossip(sha, dt, message);
                                    out.println("Inserted");
                                }
                                break;
                            }
                            case "PEER": {
                                String name = input[1];
                                String port = input[2].split(Parser.kv)[1];
                                String ip = input[3].substring(0, input[3].indexOf(Parser.nullOp)).split(Parser.kv)[1];
                                db.insertPeer(name, port, ip);
                                out.println("Inserted");
                                break;
                            }
                            case "PEERS?": {
                                String peerResponse = Parser.ParseSelectedPeers(db.selectPeers());
                                out.println(peerResponse);
                                break;
                            }
                            default:
                                out.println("Unknown Command: " + command);
                                break;
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
