package edu.cse4232.gossip.server;

import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOptsException;
import edu.cse4232.gossip.helper.DataBaseHandler;
import edu.cse4232.gossip.helper.Logger;

import java.io.IOException;
import java.util.Arrays;

public class Server {

    public static final boolean APPEND = false;
    public static final boolean DEBUG_MODE = true;

    public static final String LOG_PATH = "server.log";

    public static void main(String... args) {

        int serverPort = -1;
        StringBuilder dbConnectionString = new StringBuilder("jdbc:sqlite:");
        String host = "";
        int witness = -1;

        try (Logger log = Logger.Initialize()) {

            log.log(Logger.NOP, Logger.DRIVER, Logger.WARN, String.format("Arguments: %s", Arrays.toString(args)));

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
                        default:
                            log.log(Integer.toString(ch));
                    }
                }

                log.log(Logger.NOP, Logger.DRIVER, Logger.WARN, "Connecting To Database Instance...");
                try (DataBaseHandler db = DataBaseHandler.Initialize(dbConnectionString.toString())) {

                    log.log(Logger.NOP, Logger.DRIVER, Logger.WARN,"Recreating Tables...");
                    db.recreate();

                    Thread tcpServer = new Thread(new TCPServer(serverPort), "TCPserver");
                    Thread udpServer = new Thread(new UDPServer(serverPort), "UPDserver");

                    log.log(Logger.NOP, Logger.DRIVER, Logger.WARN,"Starting TCP Server...");
                    tcpServer.start();
                    log.log(Logger.NOP, Logger.DRIVER, Logger.WARN,"Starting UPD Server...");
                    udpServer.start();

                    tcpServer.join();
                    udpServer.join();

                } catch (Exception ex) {
                    log.log(ex);
                }
            } catch (GetOptsException e) {
                log.log(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
