package com.cse4232.gossip.udp;

import com.cse4232.gossip.GossipClient;
import com.cse4232.gossip.helper.Logger;
import com.cse4232.gossip.helper.asn.Gossip;
import com.cse4232.gossip.helper.asn.Peer;
import com.cse4232.gossip.helper.asn.PeersAnswer;
import com.cse4232.gossip.helper.asn.PeersQuery;
import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASN1_Util;
import net.ddp2p.ASN1.Decoder;

import java.io.IOException;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Calendar;

public class UDPClient implements GossipClient {

    private static final int PACKET_SIZE = 512;

    private static int portCount = 2346;

    private DatagramSocket udpSocket;
    private SocketAddress address;

    private String host;
    private int port;

    private Logger log;


    public UDPClient(String host, int port) throws Exception {
        this.udpSocket = new DatagramSocket(new InetSocketAddress("localhost", ++portCount));
        this.address = new InetSocketAddress(host, port);
        this.host = host;
        this.port = port;
        this.log = Logger.getInstance();

        if (log == null) log = Logger.Initialize("src/client.log", false, true);

        log.log(Logger.UDP, Logger.CLIENT, Logger.WARN, String.format("Sending to %s:%d", host, port));
    }

    public void sendGossip(String message) throws NoSuchAlgorithmException, IOException {

        Calendar timestamp = ASN1_Util.CalendargetInstance();
        String fullMessage = ASN1_Util.getStringDate(timestamp) + ":" + message;

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] digest = messageDigest.digest(fullMessage.getBytes());
        String hash = Base64.getEncoder().encodeToString(digest);

        Gossip gossip = new Gossip(hash, timestamp, message);
        byte[] out = gossip.encode();

        DatagramPacket packet = new DatagramPacket(out, out.length, address);
        udpSocket.send(packet);

        log.log(Logger.UDP, Logger.CLIENT, Logger.SENT, gossip.toString());
    }

    public void sendPeer(String name, String ip, String port) throws IOException {

        Peer peer = new Peer(name, Integer.parseInt(port), ip);
        byte[] out = peer.encode();

        DatagramPacket packet = new DatagramPacket(out, out.length, address);
        udpSocket.send(packet);

        log.log(Logger.UDP, Logger.CLIENT, Logger.SENT, peer.toString());
    }

    public Peer[] getPeers() throws IOException, ASN1DecoderFail {

        PeersQuery peersQuery = new PeersQuery();
        byte[] out = peersQuery.encode();

        DatagramPacket sendPacket = new DatagramPacket(out, out.length, address);
        udpSocket.send(sendPacket);

        log.log(Logger.UDP, Logger.CLIENT, Logger.SENT, peersQuery.toString());

        byte[] in = new byte[PACKET_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(in, PACKET_SIZE);
        udpSocket.receive(receivePacket);

        Decoder decoder = new Decoder(in);
        if (decoder.getTypeByte() != PeersAnswer.TAG) {
            log.log("Wrong Tag");
        }

        PeersAnswer peersAnswer = new PeersAnswer();
        peersAnswer.decode(decoder);

        log.log(Logger.UDP, Logger.CLIENT, Logger.RECV, peersAnswer.toString());

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

    @Override
    public void close() { udpSocket.close(); }

}
