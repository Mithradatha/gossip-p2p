package com.cse4232.gossip;

import com.cse4232.gossip.helper.asn.Gossip;
import com.cse4232.gossip.helper.asn.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class Broadcaster {

    private DatagramSocket socket;

    public Broadcaster(DatagramSocket socket) { this.socket = socket; }

    public void broadcast(Peer[] peers, Gossip gossip) throws IOException {

        for (Peer peer : peers) {

            String host = peer.getIp();
            int port = peer.getPort();

            InetSocketAddress address = new InetSocketAddress(host, port);

            byte[] out = gossip.encode();
            DatagramPacket packet = new DatagramPacket(out, out.length, address);

            socket.send(packet);
        }
    }
}
