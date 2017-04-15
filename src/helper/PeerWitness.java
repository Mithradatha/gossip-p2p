package com.cse4232.gossip.helper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class PeerWitness {

    private static final int PACKET_SIZE = 512;

    public static void main(String... args) {

        int port = Integer.parseInt(args[0]);

        try (DatagramSocket sock = new DatagramSocket(port)) {

            while(true) {

                DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
                sock.receive(packet);
                System.out.print(new String(packet.getData(), "UTF-8"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
