package test;

import com.cse4232.gossip.helper.asn.Gossip;
import com.cse4232.gossip.helper.asn.Peer;
import com.cse4232.gossip.helper.asn.PeersQuery;
import net.ddp2p.ASN1.ASN1_Util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Calendar;

class UDPTest {

    public static void main(String... args) {
        try {
            DatagramSocket sock = new DatagramSocket();
            Peer peer = new Peer("Sam", 1234, "172.349.123213");
            byte[] out = peer.encode();
            DatagramPacket sentPacket = new DatagramPacket(out, out.length, new InetSocketAddress("localhost", 2345));
            sock.send(sentPacket);

            Thread.sleep(1000);

            peer = new Peer("Mary", 2347, "192.921.123213");
            byte[] out2 = peer.encode();
            sentPacket = new DatagramPacket(out2, out2.length, new InetSocketAddress("localhost", 2345));
            sock.send(sentPacket);

            Thread.sleep(1000);

            PeersQuery query = new PeersQuery();
            out = query.encode();
            sentPacket = new DatagramPacket(out, out.length, new InetSocketAddress("localhost", 2345));
            sock.send(sentPacket);

            Thread.sleep(1000);

            String fullMessage = "";
            String message = "This is gossip";
            Calendar timestamp = ASN1_Util.CalendargetInstance();
            /*String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSSz")
                        .format(new Timestamp(System.currentTimeMillis()));*/
            fullMessage += ASN1_Util.getStringDate(timestamp);
            fullMessage += ":" + message;

            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(fullMessage.getBytes());
            String hash = Base64.getEncoder().encodeToString(digest);
            Gossip gossip = new Gossip(hash, timestamp, message);

            byte[] out3 = gossip.encode();
            sentPacket = new DatagramPacket(out3, out3.length, new InetSocketAddress("localhost", 2345));
            sock.send(sentPacket);

            sock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
