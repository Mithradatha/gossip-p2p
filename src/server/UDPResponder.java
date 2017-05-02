package edu.cse4232.gossip.server;

import edu.cse4232.gossip.asn1.*;
import edu.cse4232.gossip.context.Context;
import edu.cse4232.gossip.context.ContextException;
import net.ddp2p.ASN1.Decoder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.sql.SQLException;

/**
 * Handles UDP Clients
 */
public class UDPResponder extends Responder {

    private DatagramSocket socket;
    private DatagramPacket packet;

    /**
     * @param packet Received UDP Packet
     * @param context Server Context
     * @throws ContextException Server Assets Uninitialized
     * @throws SocketException I/O Unavailable
     */
    public UDPResponder(DatagramPacket packet, Context context) throws ContextException, SocketException {
        super(packet.getAddress().getCanonicalHostName(), packet.getPort(), context);
        this.socket = new DatagramSocket();
        this.packet = packet;
    }

    /**
     * Selects Known Peers from Database
     * Encodes New PeerAnswer
     * Sends PeerAnswer Packet to Client
     */
    @Override
    public void handlePeersQuery() {

        try {

            Peer[] peers = getDataBaseHandler().selectPeers();

            PeersAnswer peersAnswer = new PeersAnswer(peers);
            byte[] out = peersAnswer.encode();

            DatagramPacket datagramPacket = new DatagramPacket(out, out.length, packet.getSocketAddress());
            socket.send(datagramPacket);

        } catch (SQLException ex) {
            System.err.println("SQL Exception");
        } catch (IOException e) {
            System.err.println("UDP Packet Send Error");
        }
    }

    /**
     * Serves Individual UDP Packets
     */
    @Override
    public void run() {

        byte[] buffer = packet.getData();
        Decoder decoder = new Decoder(buffer);

        byte type = decoder.getTypeByte();

        if (type == Gossip.TAG) {
            handleGossip(decoder);
        } else if (type == Peer.TAG) {
            handlePeer(decoder);
        } else if (type == PeersQuery.TAG) {
            handlePeersQuery();
        } else if (type == Leave.TAG) {
            handleLeave(decoder);
        } else {
            System.err.println("Unknown Tag");
        }

        resetPeerTimeout();

        close();
    }

    /**
     * Closes UDP Socket
     */
    @Override
    public void close() {
        socket.close();
    }
}
