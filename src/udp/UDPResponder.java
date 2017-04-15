package com.cse4232.gossip.udp;

import com.cse4232.gossip.helper.DataBaseHandler;
import com.cse4232.gossip.helper.Logger;
import com.cse4232.gossip.helper.asn.Gossip;
import com.cse4232.gossip.helper.asn.Peer;
import com.cse4232.gossip.helper.asn.PeersAnswer;
import com.cse4232.gossip.helper.asn.PeersQuery;
import net.ddp2p.ASN1.ASN1_Util;
import net.ddp2p.ASN1.Decoder;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

class UDPResponder implements Runnable {

    private final DatagramSocket udpSocket;
    private final DatagramPacket packet;

    private final Logger log;
    private final DataBaseHandler db;

    public UDPResponder(DatagramSocket udpSocket, DatagramPacket packet) {
        this.udpSocket = udpSocket;
        this.packet = packet;
        this.log = Logger.getInstance();
        this.db = DataBaseHandler.getInstance();
    }

    @Override
    public void run() {

        byte[] in = packet.getData();
        Decoder decoder = new Decoder(in);

        byte type = decoder.getTypeByte();

        try {

            switch(type) {

                case Gossip.TAG:
                    Gossip gossip = new Gossip();
                    gossip.decode(decoder);
                    log.log(Logger.UDP, Logger.SERVER, Logger.RECV, gossip.toString());
                    String hash = gossip.getSha256hash();
                    String dt = ASN1_Util.getStringDate(gossip.getTimestamp());
                    String message = gossip.getMessage();
                    db.insertGossip(hash, dt, message);
                    break;

                case Peer.TAG:
                    Peer peer = new Peer();
                    peer.decode(decoder);
                    log.log(Logger.UDP, Logger.SERVER, Logger.RECV, peer.toString());
                    String name = peer.getName();
                    String ip = peer.getIp();
                    String port = Integer.toString(peer.getPort());
                    db.insertPeer(name, port, ip);
                    break;

                case PeersQuery.TAG:
                    PeersQuery peersQuery = new PeersQuery();
                    log.log(Logger.UDP, Logger.SERVER, Logger.RECV, peersQuery.toString());

                    Peer[] peers = db.selectPeers();
                    PeersAnswer peersAnswer = new PeersAnswer(peers);
                    log.log(Logger.UDP, Logger.SERVER, Logger.SENT, peersAnswer.toString());

                    byte[] out = peersAnswer.encode();
                    DatagramPacket datagramPacket = new DatagramPacket(out, out.length, packet.getSocketAddress());
                    udpSocket.send(datagramPacket);
                    break;

                default:
                    log.log("Incorrect Data Tag");
                    System.exit(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
