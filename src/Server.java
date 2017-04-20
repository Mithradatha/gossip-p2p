package com.cse4232.gossip;

import com.cse4232.gossip.helper.DataBaseHandler;
import com.cse4232.gossip.helper.Logger;
import com.cse4232.gossip.tcp.TCPServer;
import com.cse4232.gossip.udp.UDPServer;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOptsException;

import java.io.IOException;
import java.util.Arrays;

class Server {

    private static final boolean APPEND = false;
    private static final boolean DEBUG_MODE = true;

    private static final String LOG_PATH = "src/server.log";

    public static void main(String... args) {

        int serverPort = -1;
        String dbConnectionString = "jdbc:sqlite:";
        String host = "";
        int witness = -1;

        try (Logger log = Logger.Initialize(LOG_PATH, APPEND, DEBUG_MODE)) {

            log.log(Logger.NOP, Logger.DRIVER, Logger.WARN, String.format("Arguments: %s", Arrays.toString(args)));

            GetOpt g = new GetOpt(args, "p:d:h:w:");
            int ch;
            try {

                while ((ch = g.getNextOption()) != -1) {

                    switch (ch) {
                        case 'p':
                            serverPort = Integer.parseInt(g.getOptionArg());
                            break;
                        case 'd':
                            dbConnectionString += g.getOptionArg();
                            break;
                        case 'h':
                            host = g.getOptionArg();
                            break;
                        case 'w':
                            witness = Integer.parseInt(g.getOptionArg());
                            break;
                        default:
                            log.log(Integer.toString(ch));
                    }
                }

                log.log(Logger.NOP, Logger.DRIVER, Logger.WARN, "Connecting To Database Instance...");
                try (DataBaseHandler db = DataBaseHandler.Initialize(dbConnectionString)) {

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
