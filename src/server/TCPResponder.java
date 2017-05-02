package edu.cse4232.gossip.server;

import edu.cse4232.gossip.asn1.*;
import edu.cse4232.gossip.context.*;
import net.ddp2p.ASN1.Decoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;

/**
 * Handles TCP Clients
 */
public class TCPResponder extends Responder {

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    /**
     * @param client TCP Socket
     * @param context Server Context
     * @throws ContextException Server Assets Uninitialized
     * @throws IOException I/O Unavailable
     */
    public TCPResponder(Socket client, Context context) throws ContextException, IOException {
        super(client.getLocalAddress().getCanonicalHostName(), client.getLocalPort(), context);
        this.socket = client;
        socket.setSoTimeout(Server.TIME_OUT);
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    /**
     * Selects Known Peers from Database
     * Encodes New PeerAnswer
     * Streams PeerAnswer to Client
     */
    @Override
    public void handlePeersQuery() {

        try {

            Peer[] peers = getDataBaseHandler().selectPeers();

            PeersAnswer peersAnswer = new PeersAnswer(peers);
            byte[] out = peersAnswer.encode();

            outputStream.write(out);
            outputStream.flush();

        } catch (SQLException ex) {
            System.err.println("SQL Exception");
        } catch (IOException e) {
            System.err.println("TCP Output Stream Error");
        }
    }

    /**
     * Serves Requests while Client Connection Open
     */
    @Override
    public void run() {

        try {

            byte[] buffer = new byte[Server.PACKET_SIZE];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) > 0) {

                Decoder decoder = new Decoder(buffer, 0, bytesRead);
                if (!decoder.fetchAll(inputStream)) {
                    break;
                }

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
            }

        } catch (SocketTimeoutException e) {
            System.err.println("TCP Socket Timed Out");
        } catch (Exception ignored) {
        }

        try { close(); }
        catch (IOException e) {
            System.err.println("Failed Closing TCP Responder");
        }
    }

    /**
     * Closes I/O Streams
     * Closes TCP Socket
     * @throws IOException I/O Socket Exception
     */
    @Override
    public void close() throws IOException {
        outputStream.close();
        inputStream.close();
        socket.close();
    }
}