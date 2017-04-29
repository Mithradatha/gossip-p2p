package edu.cse4232.gossip.server;

import edu.cse4232.gossip.asn1.*;
import edu.cse4232.gossip.context.*;
import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.Decoder;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Base Class for UTP/TCP Responders
 */
abstract class Responder implements Runnable, Closeable {

    private DataBaseHandler dataBaseHandler;
    private Broadcaster broadcaster;
    private Logger logger;

    DataBaseHandler getDataBaseHandler() {
        return dataBaseHandler;
    }

    public Broadcaster getBroadcaster() {
        return broadcaster;
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * @param context Server Assets
     * @throws ContextException Uninitialized Object
     */
    Responder(Context context) throws ContextException {
        this.dataBaseHandler = context.getDataBaseHandler();
        this.broadcaster = context.getBroadcaster();
        this.logger = context.getLogger();
    }

    /**
     * Depends on Socket Type of Responder
     */
    public abstract void handlePeersQuery();

    /**
     * Decodes Gossip Messages
     * Inserts New Messages into Database
     * Broadcasts New Gossip to Known Peers
     * @param decoder Gossip Decoder
     */
    void handleGossip(Decoder decoder) {

        Gossip gossip = new Gossip();

        try {

            gossip.decode(decoder);

            String hash = gossip.getSha256hash();
            String dt = gossip.getTimestamp();
            String message = gossip.getMessage();

            if (dataBaseHandler.exists(hash)) {
                System.err.println("DISCARDED");

            } else {
                dataBaseHandler.insertGossip(hash, dt, message);
                Peer[] peers = dataBaseHandler.selectPeers();
                broadcaster.broadcast(peers, gossip);
            }

        } catch (ASN1DecoderFail asn1DecoderFail) {
            System.err.println("Gossip Decoding Failed");
        } catch (SQLException e) {
            System.err.println("SQLException");
        } catch (IOException e) {
            System.err.println("Broadcast Failed");
        }
    }

    /**
     * Decodes Peer Messages
     * Upserts Peer into Database
     * @param decoder Peer Decoder
     */
    void handlePeer(Decoder decoder) {

        Peer peer = new Peer();

        try {

            peer.decode(decoder);

            String name = peer.getName();
            String ip = peer.getIp();
            String port = Integer.toString(peer.getPort());

            dataBaseHandler.insertPeer(name, port, ip);

        } catch (SQLException e) {
            System.err.println("SQLException");
        } catch (ASN1DecoderFail asn1DecoderFail) {
            System.err.println("Peer Decoding Failed");
        }
    }

    /**
     * Decodes Leave Messages
     * Deletes Peer from Database
     * @param decoder
     */
    void handleLeave(Decoder decoder) {

        Leave leave = new Leave();

        try {
            leave.decode(decoder);

            String user = leave.getName();
            dataBaseHandler.removeUser(user);

        } catch (ASN1DecoderFail asn1DecoderFail) {
            System.out.println("Leave Decoding Failed");
        } catch (SQLException e) {
            System.err.println("SQL Exception");
        }
    }
}
