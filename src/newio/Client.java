package com.cse4232.gossip.newio;

import com.cse4232.gossip.helper.DataBaseHandler;
import com.cse4232.gossip.helper.Logger;
import com.cse4232.gossip.helper.Parser;
import com.cse4232.gossip.helper.asn.Gossip;
import com.cse4232.gossip.helper.asn.Peer;
import com.cse4232.gossip.helper.asn.PeersAnswer;
import com.cse4232.gossip.helper.asn.PeersQuery;
import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.Decoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.sql.SQLException;

public class Client implements Runnable {

    private static final int BUFFER_SIZE = 512;

    private Socket tcpSocket;
    private InputStream in;
    private OutputStream out;

    private DatagramSocket udpSocket;
    private SocketAddress address;
    private byte[] data;

    public Client(Socket client) throws IOException {
        this.tcpSocket = client;
        this.in = client.getInputStream();
        this.out = client.getOutputStream();
    }

    public Client(DatagramSocket client, DatagramPacket packet) {
        this.udpSocket = client;
        this.address = packet.getSocketAddress();
        this.data = packet.getData();
    }

    @Override
    public void run() {
        if (udpSocket != null) {
            Decoder decoder = new Decoder(data);
            byte type = decoder.getTypeByte();
            try {
                switch (type) {
                    case PeersQuery.TAG:
                        Peer[] peers = DataBaseHandler.getInstance().selectPeers();
                        PeersAnswer rs = new PeersAnswer(peers);
                        byte[] buffer = rs.encode();
                        DatagramPacket sentPacket = new DatagramPacket(buffer, buffer.length, address);
                        udpSocket.send(sentPacket);
                        break;
                    case Gossip.TAG: break;
                    case Peer.TAG:
                        Peer peer = new Peer();
                        peer.decode(decoder);
                        //TODO: Parser constants -> Logger constants
                        DataBaseHandler.getInstance().insertPeer(peer.getName(), Integer.toString(peer.getPort()), peer.getIp());
                        Logger.getInstance().log(Parser.UDP, Parser.CLIENT,
                                String.format("PEER:%s:PORT=%s:IP=%s%%", peer.getName(), peer.getPort(), peer.getIp()));
                        break;
                    case PeersAnswer.TAG: break;
                    default: break;
                }
            } catch (ASN1DecoderFail | SQLException | IOException e) {
                e.printStackTrace();
            }
        } else {
            for (; ; ) {
                try {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead = in.read(buffer);
                    if (bytesRead <= 0) break;
                    Decoder decoder = new Decoder(buffer);
                    if (decoder.fetchAll(in)) {
                        byte type = decoder.getTypeByte();
                        if (type == Peer.TAG) {
                            Peer peer = new Peer();
                            peer.decode(decoder);
                            //TODO: Parser constants -> Logger constants
                            Logger.getInstance().log(Parser.UDP, Parser.CLIENT,
                                    String.format("PEER:%s:PORT=%s:IP=%s%%", peer.getName(), peer.getPort(), peer.getIp()));
                        }
                    }
                } catch (IOException | ASN1DecoderFail e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
