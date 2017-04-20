package com.cse4232.gossip;

import com.cse4232.gossip.tcp.TCPClient;
import com.cse4232.gossip.udp.UDPClient;

abstract class GossipClientFactory {

    static GossipClient makeClient(String host, int port, int type) throws Exception {

        if      (type == GossipClient.UDP) return new UDPClient(host, port);
        else if (type == GossipClient.TCP) return new TCPClient(host, port);
        else throw new Exception("GossipClient type must be either TCP or UDP");
    }
}
