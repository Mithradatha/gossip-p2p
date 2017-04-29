package edu.cse4232.gossip.server;

import edu.cse4232.gossip.helper.Logger;

import java.io.IOException;
import java.net.*;

class UDPServer implements Runnable, AutoCloseable {

    private static final int PACKET_SIZE = 512;

    private DatagramSocket udpServer;
    private Logger log;

    public UDPServer(int port) throws SocketException {
        this.udpServer = new DatagramSocket(port);
        this.log = Logger.getInstance();
    }

    /**
     * Closes UDP Socket
     * @throws Exception Socket.close()
     */
    @Override
    public void close() throws Exception {
        if (udpServer != null) udpServer.close();
    }

    /**
     * Receives UDP Client Requests
     * Generates new Thread to Handle Client
     */
    @Override
    public void run() {

        for (;;) {

            try {

                DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
                udpServer.receive(packet);
                new Thread(new UDPResponder(packet)).start();
                log.log(Logger.UDP, Logger.SERVER, Logger.WARN, String.format("Received from %s", packet.getSocketAddress()));

            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
