package com.cse4232.gossip.tcp;

import com.cse4232.gossip.helper.Logger;
import com.cse4232.gossip.helper.asn.Gossip;
import com.cse4232.gossip.helper.asn.Peer;
import com.cse4232.gossip.helper.asn.PeersAnswer;
import com.cse4232.gossip.helper.asn.PeersQuery;
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

public class TCPClient {

    private static final int BUFFER_SIZE = 512;

    private InputStream is;
    private OutputStream os;

    private Logger log;

    public TCPClient(String host, int port) throws IOException {
        Socket tcpSocket = new Socket(host, port);
        this.is = tcpSocket.getInputStream();
        this.os = tcpSocket.getOutputStream();
        this.log = Logger.getInstance();
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
    }

    public void sendPeer(String name, String ip, int port) throws IOException {

        Peer peer = new Peer(name, port, ip);
        byte[] out = peer.encode();

        os.write(out);
        os.flush();
    }

    public Peer[] getPeers() throws IOException, ASN1DecoderFail {

        PeersQuery peersQuery = new PeersQuery();
        byte[] out = peersQuery.encode();

        os.write(out);
        os.flush();

        PeersAnswer peersAnswer = new PeersAnswer();
        byte[] in = new byte[BUFFER_SIZE];

        int bytesRead;
        while ((bytesRead = is.read(in)) > 0) {

            Decoder decoder = new Decoder(in);
            if (decoder.fetchAll(is)) {

                peersAnswer.decode(decoder);
            }
        }

        return peersAnswer.getPeers();
    }
}
