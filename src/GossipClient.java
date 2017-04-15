package com.cse4232.gossip;


import com.cse4232.gossip.helper.asn.Peer;

public interface GossipClient {

    void sendGossip(String message) throws Exception;

    void sendPeer(String name, String ip, String port) throws Exception;

    Peer[] getPeers() throws Exception;

    String getHost();

    int getPort();

    String getType();

    void close();
}
