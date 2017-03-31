import com.cse4232.gossip.helper.asn.Peer;
import com.cse4232.gossip.helper.asn.PeersAnswer;
import com.cse4232.gossip.helper.asn.PeersQuery;
import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.Decoder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class UDPTest {

    public static void main(String... args) {
        try {
            DatagramSocket sock = new DatagramSocket();
            Peer peer = new Peer("Sam", 1234, "172.349.123213");
            byte[] out = peer.encode();
            DatagramPacket sentPacket = new DatagramPacket(out, out.length, new InetSocketAddress("localhost", 2345));
            sock.send(sentPacket);
            out = new PeersQuery().encode();
            sentPacket = new DatagramPacket(out, out.length, new InetSocketAddress("localhost", 2345));
            sock.send(sentPacket);
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            sock.receive(packet);
            byte[] response = packet.getData();
            Decoder decoder = new Decoder(response);
            PeersAnswer peersAnswer = new PeersAnswer();
            peersAnswer.decode(decoder);
            for (Peer peer0 : peersAnswer.getPeers()) {
                System.out.println(String.format("PEER:%s:PORT=%s:IP=%s%%", peer0.getName(), peer0.getPort(), peer0.getIp()));
            }
            sock.close();
        } catch (IOException | ASN1DecoderFail e) {
            e.printStackTrace();
        }
    }
}
