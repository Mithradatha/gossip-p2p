package edu.cse4232.gossip.client;

import edu.cse4232.gossip.asn1.Gossip;
import edu.cse4232.gossip.asn1.Peer;
import edu.cse4232.gossip.asn1.PeersAnswer;
import edu.cse4232.gossip.asn1.PeersQuery;
import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASN1_Util;
import net.ddp2p.ASN1.Decoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Calendar;

public class TCPClient extends GossipClient {

    private Socket sock;
    private InputStream is;
    private OutputStream os;

    private String host;
    private int port;

    // private Logger log;

    public TCPClient(String host, int port) throws IOException {
        this.sock = new Socket(host, port);
        this.is = sock.getInputStream();
        this.os = sock.getOutputStream();
        this.host = host;
        this.port = port;
        //this.log = Logger.getInstance();

        //if (log == null) log = Logger.Initialize("src/client.log", false, true);

        //log.log(Logger.TCP, Logger.CLIENT, Logger.WARN, String.format("Connecting to %s:%d", host, port));
    }

    /**
     * @param message
     * @throws NoSuchAlgorithmException SHA-256
     * @throws IOException Socket Write
     */
    public void sendGossip(String message) throws NoSuchAlgorithmException, IOException {

        Calendar timestamp = ASN1_Util.CalendargetInstance();
        String fullMessage = ASN1_Util.getStringDate(timestamp) + ":" + message;

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] digest = messageDigest.digest(fullMessage.getBytes());
        //String hash = Base64.getEncoder().encodeToString(digest);

        Gossip gossip = new Gossip(digest, timestamp, message);
        byte[] out = gossip.encode();

        os.write(out);
        os.flush();

        //log.log(Logger.TCP, Logger.CLIENT, Logger.SENT, gossip.toString());
    }

    /**
     * @param name
     * @param ip
     * @param port
     * @throws IOException Socket Write
     */
    public void sendPeer(String name, String ip, String port) throws IOException {

        Peer peer = new Peer(name, Integer.parseInt(port), ip);
        byte[] out = peer.encode();

        os.write(out);
        os.flush();

        //log.log(Logger.TCP, Logger.CLIENT, Logger.SENT, peer.toString());
    }

    /**
     * Sends PeersQuery Request
     * Receives PeersAnswer Response
     * @return Known Peers
     * @throws IOException Socket Write
     * @throws ASN1DecoderFail PeersAnswer Decoder
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Peer[] getPeers() throws IOException, ASN1DecoderFail {

        PeersQuery peersQuery = new PeersQuery();
        byte[] out = peersQuery.encode();

        os.write(out);
        os.flush();

       // log.log(Logger.TCP, Logger.CLIENT, Logger.SENT, peersQuery.toString());

        PeersAnswer peersAnswer = new PeersAnswer();
        byte[] in = new byte[PACKET_SIZE];

        is.read(in, 0, 1);
        //assert bytesRead == 1;
        byte type = in[0];

        is.read(in, 1, 1);
        //assert bytesRead == 1;
        byte len = in[1];

        byte[] data = new byte[len];
        is.read(data);

        System.arraycopy(data, 0, in, 2, data.length);
        Decoder decoder = new Decoder(in);
        peersAnswer.decode(decoder);

       // log.log(Logger.TCP, Logger.CLIENT, Logger.RECV, peersAnswer.toString());

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
        return "TCP";
    }

    /**
     * Closes I/O Streams and Socket Connection
     */
    public void close() {
        try {
            if (is != null) is.close();
            if (os != null) os.close();
            if (sock != null) sock.close();
        } catch (IOException ignored) {}
    }
}
