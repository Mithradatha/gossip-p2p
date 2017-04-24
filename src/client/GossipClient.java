package edu.cse4232.gossip.client;

import edu.cse4232.gossip.asn1.Peer;

/**
 * All Clients extend this abstract class
 */
abstract class GossipClient {

    static final int UDP = 0;
    static final int TCP = 1;

    static final int PACKET_SIZE = 512;

    abstract void sendGossip(String message) throws Exception;

    abstract void sendPeer(String name, String ip, String port) throws Exception;

    abstract Peer[] getPeers() throws Exception;

    abstract String getHost();

    abstract int getPort();

    abstract String getType();

    abstract void close();
}
