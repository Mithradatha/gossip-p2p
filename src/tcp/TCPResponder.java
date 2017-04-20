package com.cse4232.gossip.tcp;

import com.cse4232.gossip.Broadcaster;
import com.cse4232.gossip.helper.DataBaseHandler;
import com.cse4232.gossip.helper.Logger;
import com.cse4232.gossip.helper.asn.*;
import net.ddp2p.ASN1.ASN1_Util;
import net.ddp2p.ASN1.Decoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

class TCPResponder implements Runnable {

    private static final int BUFFER_SIZE = 512;
    private static final int TIME_OUT = 20*1000;

    private final Socket sock;
    private InputStream is;
    private OutputStream os;

    private Logger log;
    private DataBaseHandler db;

    public TCPResponder(Socket tcpSocket) throws IOException {
        this.sock = tcpSocket;
        sock.setSoTimeout(TIME_OUT);
        this.is = sock.getInputStream();
        this.os = sock.getOutputStream();
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
                            log.log(Logger.TCP, Logger.SERVER, Logger.RECV, gossip.toString());
                            String hash = gossip.getSha256hash();
                            String dt = ASN1_Util.getStringDate(gossip.getTimestamp());
                            String message = gossip.getMessage();
                            if (db.exists(hash)) System.err.println("DISCARDED");
                            else {

                                db.insertGossip(hash, dt, message);
                                Peer[] peers = db.selectPeers();
                                DatagramSocket sock = new DatagramSocket(new InetSocketAddress("localhost", 2567));
                                Broadcaster broadcaster = new Broadcaster(sock);
                                broadcaster.broadcast(peers, gossip);
                            }
                            break;

                        case Peer.TAG:
                            Peer peer = new Peer();
                            peer.decode(decoder);
                            log.log(Logger.TCP, Logger.SERVER, Logger.RECV, peer.toString());
                            String name = peer.getName();
                            String ip = peer.getIp();
                            String port = Integer.toString(peer.getPort());

                            db.insertPeer(name, port, ip);
                            break;

                        case PeersQuery.TAG:
                            PeersQuery peersQuery = new PeersQuery();
                            log.log(Logger.TCP, Logger.SERVER, Logger.RECV, peersQuery.toString());

                            Peer[] peers = db.selectPeers();

                            PeersAnswer peersAnswer = new PeersAnswer(peers);
                            log.log(Logger.TCP, Logger.SERVER, Logger.SENT, peersAnswer.toString());

                            byte[] out = peersAnswer.encode();
                            os.write(out);
                            os.flush();
                            break;

                        case Leave.TAG:
                            Leave leave = new Leave();
                            leave.decode(decoder);
                            String user = leave.getName();
                            db.removeUser(user);
                            break;
                        default:
                            log.log("Incorrect Data Tag");
                    }
                }
            } catch (SocketTimeoutException e) {
                log.log(Logger.TCP, Logger.SERVER, Logger.WARN, e.getLocalizedMessage());
                break;
            } catch (Exception ignored) { break; }
        }

        try {
            is.close();
            os.close();
            sock.close();
            log.log(Logger.TCP, Logger.SERVER, Logger.WARN, "Socket Closed");
        } catch (IOException e) {
            log.log(e);
        }
    }
}