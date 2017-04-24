package edu.cse4232.gossip.helper;

import edu.cse4232.gossip.asn1.Gossip;
import edu.cse4232.gossip.asn1.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * Used by Server for One-Way Communication with Peers
 */
public class Broadcaster {

    private final DatagramSocket socket;

    public Broadcaster(DatagramSocket socket) { this.socket = socket; }

    /**
     * Updates Known Peers of New Gossip
     * @param peers
     * @param gossip
     * @throws IOException Socket Send DatagramPacket
     */
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
