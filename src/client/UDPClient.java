package edu.cse4232.gossip.client;

import edu.cse4232.gossip.asn1.Gossip;
import edu.cse4232.gossip.asn1.Peer;
import edu.cse4232.gossip.asn1.PeersAnswer;
import edu.cse4232.gossip.asn1.PeersQuery;
import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASN1_Util;
import net.ddp2p.ASN1.Decoder;

import java.io.IOException;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

/**
 * UDP Client
 */
class UDPClient extends GossipClient {

    private DatagramSocket udpSocket;
    private SocketAddress address;

    private String host;
    private int port;

    // private Logger log;


    public UDPClient(String host, int port) throws Exception {
        this.udpSocket = new DatagramSocket();
        this.host = host;
        this.port = port;
        this.address = new InetSocketAddress(host, port);
       // this.log = Logger.getInstance();

        //if (log == null) log = Logger.Initialize("src/client.log", false, true);

//        log.log(Logger.UDP, Logger.CLIENT, Logger.WARN, String.format("Sending to %s:%d", host, port));
    }

    /**
     * @param message
     * @throws NoSuchAlgorithmException SHA-256
     * @throws IOException Socket Send DatagramPacket
     */
    public void sendGossip(String message) throws NoSuchAlgorithmException, IOException {

        Calendar timestamp = ASN1_Util.CalendargetInstance();
        String fullMessage = Gossip.timestampToString(timestamp) + ":" + message;

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] digest = messageDigest.digest(fullMessage.getBytes());
        //String hash = Base64.getEncoder().encodeToString(digest);

        Gossip gossip = new Gossip(digest, timestamp, message);
        byte[] out = gossip.encode();

        DatagramPacket packet = new DatagramPacket(out, out.length, address);
        udpSocket.send(packet);

      //  log.log(Logger.UDP, Logger.CLIENT, Logger.SENT, gossip.toString());
    }

    /**
     * @param name
     * @param ip
     * @param port
     * @throws IOException Socket Send DatagramPacket
     */
    public void sendPeer(String name, String ip, String port) throws IOException {

        Peer peer = new Peer(name, Integer.parseInt(port), ip);
        byte[] out = peer.encode();

        DatagramPacket packet = new DatagramPacket(out, out.length, address);
        udpSocket.send(packet);

       // log.log(Logger.UDP, Logger.CLIENT, Logger.SENT, peer.toString());
    }

    /**
     * Sends PeersQuery Request
     * Receives PeersAnswer Response
     * @return Known Peers
     * @throws IOException Socket Send/Receive DatagramPacket
     * @throws ASN1DecoderFail PeersAnswer Decoder
     */
    public Peer[] getPeers() throws IOException, ASN1DecoderFail {

        PeersQuery peersQuery = new PeersQuery();
        byte[] out = peersQuery.encode();

        DatagramPacket sendPacket = new DatagramPacket(out, out.length, address);
        udpSocket.send(sendPacket);

      //  log.log(Logger.UDP, Logger.CLIENT, Logger.SENT, peersQuery.toString());

        DatagramPacket receivePacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
        udpSocket.receive(receivePacket);

        Decoder decoder = new Decoder(receivePacket.getData());
        /*if (decoder.getTypeByte() != PeersAnswer.TAG) {
           // log.log("Wrong Tag");
        }*/

        PeersAnswer peersAnswer = new PeersAnswer();
        peersAnswer.decode(decoder);

        //log.log(Logger.UDP, Logger.CLIENT, Logger.RECV, peersAnswer.toString());

        return peersAnswer.getPeers();
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getType() {
        return "UDP";
    }

    /**
     * Closes UDP Socket
     */
    @Override
    public void close() { udpSocket.close(); }

}
