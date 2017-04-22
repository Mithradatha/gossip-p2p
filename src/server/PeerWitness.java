package edu.cse4232.gossip.server;

import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;
import edu.cse4232.gossip.asn1.Gossip;
import edu.cse4232.gossip.asn1.Peer;
import edu.cse4232.gossip.helper.Logger;
import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASN1_Util;
import net.ddp2p.ASN1.Decoder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

class PeerWitness {

    private static final int PACKET_SIZE = 512;

    public static void main(String... args) {

        String serverHost = "";
        int serverPort = 0;

            GetOpt g = new GetOpt(args, "h:p:");

            int ch;
            try {
                while ((ch = g.getNextOption()) != -1) {
                    switch (ch) {
                        case 'h':
                            serverHost = g.getOptionArg();
                            break;
                        case 'p':
                            serverPort = Integer.parseInt(g.getOptionArg());
                            break;
                    }
                }
            } catch (Exception ignored) {}


        try (DatagramSocket sock = new DatagramSocket()) {

            Peer peer = new Peer("Witness", sock.getLocalPort(), "localhost");
            byte[] out = peer.encode();

            DatagramPacket peerNotify = new DatagramPacket(out, out.length, new InetSocketAddress(serverHost, serverPort));
            sock.send(peerNotify);

            while(true) {

                DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
                sock.receive(packet);

                byte[] in = packet.getData();
                Decoder decoder = new Decoder(in);

                byte type = decoder.getTypeByte();
                if (type == Gossip.TAG) {
                    Gossip gossip = new Gossip();
                    gossip.decode(decoder);
                    System.out.println(gossip);
                }
            }

        } catch (IOException | ASN1DecoderFail e) {
            e.printStackTrace();
        }
    }
}
