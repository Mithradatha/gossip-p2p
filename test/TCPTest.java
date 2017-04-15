package test;

import com.cse4232.gossip.helper.asn.Peer;

import java.io.IOException;
import java.net.Socket;

class TCPTest {

    public static void main(String... args) {
        try {
            Socket client = new Socket("localhost", 2345);
            Peer peer = new Peer("Sam", 1234, "172.349.123213");
            byte[] out = peer.encode();
            client.getOutputStream().write(out);
            client.getOutputStream().flush();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
