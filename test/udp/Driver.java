package test.udp;

import com.cse4232.gossip.helper.asn.Peer;
import com.cse4232.gossip.udp.UDPClient;
import net.ddp2p.ASN1.ASN1DecoderFail;

import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Driver {

    public static void main(String... args) {

        String host = "localhost";
        int port = 2345;

        List<String[]> peers = new ArrayList<String[]>();
        peers.add(new String[] {"Sam", "172.23.12.123", "1234"});

        List<String> gossips = new ArrayList<String>();
        gossips.add("This is gossip");

        try {

            UDPClient client = new UDPClient(host, port);

            for (String[] peer : peers) {
                client.sendPeer(peer[0], peer[1], (peer[2]));
            }

            for (String gossip : gossips) {
                client.sendGossip(gossip);
            }

            Peer[] knownPeers = client.getPeers();
            System.out.println(Arrays.toString(knownPeers));

        } catch (java.io.IOException | NoSuchAlgorithmException | ASN1DecoderFail e) {
            e.printStackTrace();
        }

    }
}
