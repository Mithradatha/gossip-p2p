package edu.cse4232.gossip.server;

import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOptsException;
import edu.cse4232.gossip.context.*;

import java.io.IOException;
import java.net.*;
import java.sql.SQLException;

/**
 * Gossip Server
 */
class Server implements Runnable {

    public static final int PACKET_SIZE = 512;
    public static final int TIME_OUT = 1000*20; // 20 seconds

    private static final boolean APPEND = false;
    private static final boolean DEBUG_MODE = true;
    private static final String LOG_PATH = "server.log";

    private final Context context;
    private final int port;

    private Server(int port, Context context) {
        this.context = context;
        this.port = port;
    }

    /**
     * @param args -p [port] -d [database file]
     */
    public static void main(String... args) {

        int serverPort = -1;
        StringBuilder dbConnectionString = new StringBuilder("jdbc:sqlite:");

        GetOpt g = new GetOpt(args, "p:d:");
        int ch;

        try {
            while ((ch = g.getNextOption()) != -1) {

                switch (ch) {
                    case 'p':
                        serverPort = Integer.parseInt(g.getOptionArg());
                        break;
                    case 'd':
                        dbConnectionString.append(g.getOptionArg());
                        break;
                }
            }
        } catch (GetOptsException ignored) {
            System.err.println("Invalid Command Line Arguments");
        }

        try {

            Context ctx = new ContextBuilder()
                    .setDataBaseHandler(new DataBaseHandler(dbConnectionString.toString()))
                    .setBroadcaster(new Broadcaster())
                    .setLogger(new Logger(LOG_PATH, APPEND, DEBUG_MODE))
                    .createContext();

            try {
                ctx.getDataBaseHandler().recreate();
            } catch (ContextException ignored) {
                System.err.println("DatabaseHandler Null in Context");
            }

            Server server = new Server(serverPort, ctx);
            server.run();

        } catch (SocketException ignored) {
            System.err.println("Broadcaster Socket Initialization Failed");
        } catch (IOException ignored) {
            System.err.println("Logger File Initialization Failed");
        } catch (SQLException ignored) {
            System.err.println("Database Handler Initialization Failed");
        }
    }

    /**
     * Creates 2 Server Threads (UDP, TCP)
     * Listens for Clients
     * Creates New Responder Thread for each Client
     */
    @Override
    public void run() {

        Thread tcpThread = new Thread(() -> {

            try (ServerSocket tcpServer = new ServerSocket(port)) {

                while (true) {

                    Socket client = tcpServer.accept();
                    Thread clientThread = new Thread(new TCPResponder(client, context));
                    clientThread.run();
                }

            } catch (IOException ignored) {
                System.err.println("TCP Server Port Unavailable");
            } catch (ContextException e) {
                System.err.println(e.getMessage());
            }
        });

        Thread udpThread = new Thread(() -> {

            try (DatagramSocket udpSocket = new DatagramSocket(port)) {

                while (true) {

                    DatagramPacket packet = new DatagramPacket(
                            new byte[PACKET_SIZE], PACKET_SIZE);
                    udpSocket.receive(packet);
                    Thread clientThread = new Thread(new UDPResponder(packet, context));
                    clientThread.run();
                }

            } catch (SocketException ignored) {
                System.err.println("UDP Server Port Unavailable");
            } catch (IOException ignored) {
                System.err.println("UDP Socket Receive Error");
            } catch (ContextException e) {
                System.err.println(e.getMessage());
            }
        });

        tcpThread.start();
        udpThread.start();

        try {

            tcpThread.join();
            udpThread.join();

        } catch (InterruptedException e) {
            System.err.println("Server Thread Interrupted");
        }
    }
}
