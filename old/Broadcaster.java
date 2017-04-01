package com.cse4232.gossip.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class Broadcaster {

    private static Broadcaster instance;

    public static Broadcaster getInstance() {
        if (instance == null) {
            instance = new Broadcaster();
        }
        return instance;
    }

    private Broadcaster() {
    }

    public void broadcast(String message, List<String[]> peers) throws IOException {

        message += "\n";

        byte[] gossip = message.getBytes();
        int gossipLen = message.length();

        try (DatagramSocket clientSocket = new DatagramSocket()) {

            for (String[] peer : peers) {
                int port = Integer.parseInt(peer[1]);
                InetAddress addr = InetAddress.getByName(peer[2]);
                DatagramPacket sendPacket = new DatagramPacket(gossip, gossipLen, addr, port);
                clientSocket.send(sendPacket);
            }
        }
    }
}
