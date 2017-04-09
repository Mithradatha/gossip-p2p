package com.cse4232.gossip.tcp;

import com.cse4232.gossip.GossipClient;
import com.cse4232.gossip.helper.Logger;
import com.cse4232.gossip.helper.asn.Gossip;
import com.cse4232.gossip.helper.asn.Peer;
import com.cse4232.gossip.helper.asn.PeersAnswer;
import com.cse4232.gossip.helper.asn.PeersQuery;
import com.cse4232.gossip.newio.Client;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOptsException;
import jdk.internal.util.xml.impl.Input;
import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASN1_Util;
import net.ddp2p.ASN1.Decoder;
import sun.rmi.runtime.Log;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class TCPClient implements GossipClient {

    private static final int BUFFER_SIZE = 512;

    private Socket sock;
    private InputStream is;
    private OutputStream os;

    private String host;
    private int port;

    private Logger log;

    public TCPClient(String host, int port) throws IOException {
        this.sock = new Socket(host, port);
        this.is = sock.getInputStream();
        this.os = sock.getOutputStream();
        this.host = host;
        this.port = port;
        this.log = Logger.getInstance();

        log.log(Logger.TCP, Logger.CLIENT, Logger.WARN, String.format("Connecting to %s:%d", host, port));
    }

    public void sendGossip(String message) throws NoSuchAlgorithmException, IOException {

        Calendar timestamp = ASN1_Util.CalendargetInstance();
        String fullMessage = ASN1_Util.getStringDate(timestamp) + ":" + message;

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] digest = messageDigest.digest(fullMessage.getBytes());
        String hash = Base64.getEncoder().encodeToString(digest);

        Gossip gossip = new Gossip(hash, timestamp, message);
        byte[] out = gossip.encode();

        os.write(out);
        os.flush();

        log.log(Logger.TCP, Logger.CLIENT, Logger.SENT, gossip.toString());
    }

    public void sendPeer(String name, String ip, String port) throws IOException {

        Peer peer = new Peer(name, Integer.parseInt(port), ip);
        byte[] out = peer.encode();

        os.write(out);
        os.flush();

        log.log(Logger.TCP, Logger.CLIENT, Logger.SENT, peer.toString());
    }

    public Peer[] getPeers() throws IOException, ASN1DecoderFail {

        PeersQuery peersQuery = new PeersQuery();
        byte[] out = peersQuery.encode();

        os.write(out);
        os.flush();

        log.log(Logger.TCP, Logger.CLIENT, Logger.SENT, peersQuery.toString());

        PeersAnswer peersAnswer = new PeersAnswer();
        byte[] in = new byte[BUFFER_SIZE];

        is.read(in, 0, 1);
        byte type = in[0];
        //assert type == PeersAnswer.TAG;

        is.read(in, 1, 1);
        byte len = in[1];

        byte[] data = new byte[len];
        is.read(data);

        System.arraycopy(data, 0, in, 2, data.length);
        Decoder decoder = new Decoder(in);
        peersAnswer.decode(decoder);

        log.log(Logger.TCP, Logger.CLIENT, Logger.RECV, peersAnswer.toString());

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

    public void close() {
        try {
            if (is != null) is.close();
            if (os != null) os.close();
            if (sock != null) sock.close();
        } catch (IOException ignored) {}
    }
}
