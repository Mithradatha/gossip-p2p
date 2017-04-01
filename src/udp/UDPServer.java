package com.cse4232.gossip.udp;

import com.cse4232.gossip.newio.ClientHandler;

import java.io.IOException;
import java.net.*;

public class UDPServer implements Runnable, AutoCloseable {

    private static final int PACKET_SIZE = 512;

    private DatagramSocket udpServer;

    public UDPServer(int port) throws SocketException {
        this.udpServer = new DatagramSocket(port);
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

            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
