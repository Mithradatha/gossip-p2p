package com.cse4232.gossip.newio;

import java.io.IOException;
import java.net.*;

public class Server implements Runnable, AutoCloseable {

    public static final int TCP = 1;
    public static final int UDP = 2;

    private static final int PACKET_SIZE = 512;

    private ServerSocket tcpServer;
    private DatagramSocket udpServer;

    public Server(int port, int type) throws Exception {
        if (type == TCP) {
            this.tcpServer = new ServerSocket(port);
        } else if (type == UDP) {
            this.udpServer = new DatagramSocket(port);
        } else {
            throw new Exception("ERROR: TCPServer must either by TCP or UDP type");
        }
    }

    @Override
    public void close() throws Exception {
        if (tcpServer != null) tcpServer.close();
        if (udpServer != null) udpServer.close();
    }

    @Override
    public void run() {
        if (tcpServer != null) {
            for (; ; ) {
                try {
                    Socket client = tcpServer.accept();
                    new Thread(new ClientHandler(client)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
            for (; ; ) {
                try {
                    udpServer.receive(packet);
                    new Thread(new ClientHandler(udpServer, packet)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
