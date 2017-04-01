package com.cse4232.gossip;

import com.cse4232.gossip.helper.DataBaseHandler;
import com.cse4232.gossip.helper.Logger;
import com.cse4232.gossip.udp.UDPServer;

public class Driver {

    private final static boolean APPEND = false;
    private final static boolean DEBUG_MODE = true;

    public static void main(String[] args) {

        //TODO GetOpt
        int port = 2345;
        String path = "server.log";
        String dbConnectionString = "jdbc:sqlite:";

        try (
                Logger logger = Logger.Initialize(path, APPEND, DEBUG_MODE);
                DataBaseHandler db = DataBaseHandler.Initialize(dbConnectionString);
                //TCPServer tcpServer = new TCPServer(port, TCPServer.TCP);
                UDPServer udpServer = new UDPServer(port);
        ) {

            db.recreate();

            //Thread tcpThread = new Thread(tcpServer);
            Thread udpThread = new Thread(udpServer);

            //tcpThread.start();
            udpThread.start();

            //tcpThread.join();
            udpThread.join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
