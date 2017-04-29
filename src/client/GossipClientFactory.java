package edu.cse4232.gossip.client;

/**
 * Factory for Creating Clients
 */
abstract class GossipClientFactory {

    /**
     * Generates a new Client based on runtime information
     * @param host Server IP
     * @param port Server Port
     * @param type UDP or TCP Connection
     * @return GossipClient of type UDP/TCP
     * @throws Exception type in range [UDP, TCP]
     */
    static GossipClient makeClient(String host, int port, int type) throws Exception {

        if      (type == GossipClient.UDP) return new UDPClient(host, port);
        else if (type == GossipClient.TCP) return new TCPClient(host, port);
        else throw new Exception("GossipClient type must be either TCP or UDP");
    }
}
