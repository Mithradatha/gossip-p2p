package com.cse4232.gossip.udp;

import com.cse4232.gossip.helper.Logger;

import java.io.IOException;
import java.net.*;

public class UDPServer implements Runnable, AutoCloseable {

    private static final int PACKET_SIZE = 512;

    private DatagramSocket udpServer;
    private Logger log;

    public UDPServer(int port) throws SocketException {
        this.udpServer = new DatagramSocket(port);
        this.log = Logger.getInstance();
    }

    @Override
    public void close() throws Exception {
        if (udpServer != null) udpServer.close();
    }

    @Override
    public void run() {

        for (;;) {

            try {

                DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
                udpServer.receive(packet);
                new Thread(new UDPResponder(udpServer, packet)).start();
                log.log(Logger.UDP, Logger.SERVER, Logger.WARN, String.format("Received from %s", packet.getSocketAddress()));

            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
