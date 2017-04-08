package com.cse4232.gossip.tcp;

import com.cse4232.gossip.helper.DataBaseHandler;
import com.cse4232.gossip.helper.Logger;
import com.cse4232.gossip.helper.asn.Gossip;
import com.cse4232.gossip.helper.asn.Peer;
import com.cse4232.gossip.helper.asn.PeersAnswer;
import com.cse4232.gossip.helper.asn.PeersQuery;
import net.ddp2p.ASN1.ASN1_Util;
import net.ddp2p.ASN1.Decoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPResponder implements Runnable {

    private static final int BUFFER_SIZE = 512;

    private InputStream is;
    private OutputStream os;

    private Logger log;
    private DataBaseHandler db;

    public TCPResponder(Socket tcpSocket) throws IOException {
        this.is = tcpSocket.getInputStream();
        this.os = tcpSocket.getOutputStream();
        this.log = Logger.getInstance();
        this.db = DataBaseHandler.getInstance();
    }

    @Override
    public void run() {

        byte[] in = new byte[BUFFER_SIZE];

        for (; ; ) {

            try {

                int bytesRead = is.read(in);
                if (bytesRead <= 0) break;

                Decoder decoder = new Decoder(in, 0, bytesRead);
                if (decoder.fetchAll(is)) {

                    byte type = decoder.getTypeByte();

                    switch (type) {

                        case Gossip.TAG:
                            Gossip gossip = new Gossip();
                            gossip.decode(decoder);
                            String hash = gossip.getSha256hash();
                            String dt = ASN1_Util.getStringDate(gossip.getTimestamp());
                            String message = gossip.getMessage();
                            db.insertGossip(hash, dt, message);
                            log.log(Logger.TCP, Logger.SERVER, gossip.toString());
                            break;

                        case Peer.TAG:
                            Peer peer = new Peer();
                            peer.decode(decoder);
                            String name = peer.getName();
                            String ip = peer.getIp();
                            String port = Integer.toString(peer.getPort());
                            db.insertPeer(name, port, ip);
                            log.log(Logger.TCP, Logger.SERVER, peer.toString());
                            break;

                        case PeersQuery.TAG:
                            PeersQuery peersQuery = new PeersQuery();
                            log.log(Logger.TCP, Logger.SERVER, peersQuery.toString());

                            Peer[] peers = db.selectPeers();
                            PeersAnswer peersAnswer = new PeersAnswer(peers);
                            log.log(Logger.TCP, Logger.SERVER, peersAnswer.toString());

                            byte[] out = peersAnswer.encode();
                            os.write(out);
                            os.flush();
                            break;

                        default:
                            log.log("Incorrect Data Tag");
                            System.exit(1);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}