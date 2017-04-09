package com.cse4232.gossip;


import com.cse4232.gossip.helper.asn.Peer;

public interface GossipClient {

    public void sendGossip(String message) throws Exception;

    public void sendPeer(String name, String ip, String port) throws Exception;

    public Peer[] getPeers() throws Exception;
}
