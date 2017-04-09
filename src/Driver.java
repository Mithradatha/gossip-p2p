package com.cse4232.gossip;

import com.cse4232.gossip.helper.DataBaseHandler;
import com.cse4232.gossip.helper.Logger;
import com.cse4232.gossip.tcp.TCPClient;
import com.cse4232.gossip.tcp.TCPServer;
import com.cse4232.gossip.udp.UDPClient;
import com.cse4232.gossip.udp.UDPServer;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOptsException;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;

public class Driver {

    private static final boolean APPEND = false;
    private static final boolean DEBUG_MODE = true;

    private static final String LOG_PATH = "src/server.log";

    public static void main(String... args) {

        String host = "";
        int serverPort = -1;
        String dbConnectionString = "jdbc:sqlite:";
        boolean isTcp = false;
        boolean isUdp = false;

        try (Logger log = Logger.Initialize(LOG_PATH, APPEND, DEBUG_MODE)) {

            log.log(Logger.NOP, Logger.DRIVER, Logger.WARN, String.format("Arguments: %s", Arrays.toString(args)));

            GetOpt g = new GetOpt(args, "s:p:d:TU");
            int ch = -1;
            try {

                while ((ch = g.getNextOption()) != -1) {

                    switch (ch) {
                        case 's':
                            host = g.getOptionArg();
                            break;
                        case 'p':
                            serverPort = Integer.parseInt(g.getOptionArg());
                            break;
                        case 'd':
                            dbConnectionString += g.getOptionArg();
                            break;
                        case 'T':
                            isTcp = true;
                            break;
                        case 'U':
                            isUdp = true;
                            break;
                        default:
                            log.log(Integer.toString(ch));
                    }
                }

                log.log(Logger.NOP, Logger.DRIVER, Logger.WARN, "Connecting To Database Instance...");
                try (DataBaseHandler db = DataBaseHandler.Initialize(dbConnectionString)) {

                    if (isTcp == isUdp) throw new Exception("Client must be either TCP or UDP");

                    log.log(Logger.NOP, Logger.DRIVER, Logger.WARN,"Recreating Tables...");
                    db.recreate();

                    Thread tcpServer = new Thread(new TCPServer(serverPort), "TCPserver");
                    Thread udpServer = new Thread(new UDPServer(serverPort), "UPDserver");

                    log.log(Logger.NOP, Logger.DRIVER, Logger.WARN,"Starting TCP Server...");
                    tcpServer.start();
                    log.log(Logger.NOP, Logger.DRIVER, Logger.WARN,"Starting UPD Server...");
                    udpServer.start();

                    GossipClient gossipClient = isTcp? new TCPClient(host, serverPort) : new UDPClient(host, serverPort);
                    Thread client = new Thread(new Client(gossipClient), "Gossipclient");
                    client.start();

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
