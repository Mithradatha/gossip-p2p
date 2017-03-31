package com.cse4232.gossip.newio;

import com.cse4232.gossip.helper.DataBaseHandler;
import com.cse4232.gossip.helper.Logger;

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
                Server tcpServer = new Server(port, Server.TCP);
                Server udpServer = new Server(port, Server.UDP)
        ) {

            db.recreate();

            Thread tcpThread = new Thread(tcpServer);
            Thread udpThread = new Thread(udpServer);

            tcpThread.start();
            udpThread.start();

            tcpThread.join();
            udpThread.join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
