package edu.cse4232.gossip.server;

import edu.cse4232.gossip.asn1.Gossip;
import edu.cse4232.gossip.asn1.Peer;
import edu.cse4232.gossip.asn1.PeersAnswer;
import edu.cse4232.gossip.asn1.PeersQuery;
import edu.cse4232.gossip.helper.Broadcaster;
import edu.cse4232.gossip.helper.DataBaseHandler;
import edu.cse4232.gossip.helper.Logger;
import net.ddp2p.ASN1.ASN1_Util;
import net.ddp2p.ASN1.Decoder;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

class UDPResponder implements Runnable {

    // private int port = 2349;

    private final DatagramSocket udpSocket;
    private final DatagramPacket packet;

    private final Logger log;
    private final DataBaseHandler db;

    public UDPResponder(DatagramPacket packet) throws SocketException {
        //this.udpSocket = new DatagramSocket(new InetSocketAddress("localhost", PortFinder.find()));
        this.udpSocket = new DatagramSocket();
        this.packet = packet;
        this.log = Logger.getInstance();
        this.db = DataBaseHandler.getInstance();
    }

    /**
     * Handles UPP Client Packets
     */
    @Override
    public void run() {

        byte[] in = packet.getData();
        Decoder decoder = new Decoder(in);

        byte type = decoder.getTypeByte();

        try {

            if (type == Gossip.TAG) {
                Gossip gossip = new Gossip();
                gossip.decode(decoder);
                log.log(Logger.UDP, Logger.SERVER, Logger.RECV, gossip.toString());
                String hash = gossip.getSha256hash();
                String dt = gossip.getTimestamp();
                String message = gossip.getMessage();
                if (db.exists(hash)) System.err.println("DISCARDED");
                else {

                    db.insertGossip(hash, dt, message);
                    Peer[] peers = db.selectPeers();
                    DatagramSocket sock = new DatagramSocket();
                    Broadcaster broadcaster = new Broadcaster(sock);
                    broadcaster.broadcast(peers, gossip);
                }

            } else if (type == Peer.TAG) {
                Peer peer = new Peer();
                peer.decode(decoder);
                log.log(Logger.UDP, Logger.SERVER, Logger.RECV, peer.toString());
                String name = peer.getName();
                String ip = peer.getIp();
                String port = Integer.toString(peer.getPort());
                db.insertPeer(name, port, ip);

            } else if (type == PeersQuery.TAG) {
                PeersQuery peersQuery = new PeersQuery();
                log.log(Logger.UDP, Logger.SERVER, Logger.RECV, peersQuery.toString());

                Peer[] peers = db.selectPeers();
                PeersAnswer peersAnswer = new PeersAnswer(peers);
                log.log(Logger.UDP, Logger.SERVER, Logger.SENT, peersAnswer.toString());

                byte[] out = peersAnswer.encode();
                DatagramPacket datagramPacket = new DatagramPacket(out, out.length, packet.getSocketAddress());
                udpSocket.send(datagramPacket);

            } else {
                log.log(String.format("Incorrect Data Tag %s", type));
                System.exit(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
