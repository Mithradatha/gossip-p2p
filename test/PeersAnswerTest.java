import com.cse4232.gossip.helper.asn.Peer;
import com.cse4232.gossip.helper.asn.PeersAnswer;
import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.Decoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PeersAnswerTest {

    private static final int BUFFER_SIZE = 512;

    public static void main(String[] args) {

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try (
                ServerSocket server = new ServerSocket(port);
                Socket client = new Socket(host, port);
                Socket clientSocket = server.accept();
                InputStream serverInput = clientSocket.getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream()
        ) {

            Peer[] peers = new Peer[3];
            peers[0] = new Peer("Sam", 1234, "163.42");
            peers[1] = new Peer("Mary", 2346, "172.91");
            peers[2] = new Peer("John", 4567, "195.23");
            PeersAnswer peersAnswer = new PeersAnswer(peers);
            client.getOutputStream().write(peersAnswer.encode());

            byte[] msg = new byte[BUFFER_SIZE];
            Decoder decoder;
            int bytesRead;

            do {
                bytesRead = serverInput.read(msg);
                buffer.write(msg);
                decoder = new Decoder(buffer.toByteArray());
                if (bytesRead <= 0) break;
            } while (!decoder.fetchAll(serverInput));

            peersAnswer = new PeersAnswer();
            peersAnswer.decode(decoder);
            for (Peer peer : peersAnswer.getPeers()) {
                System.out.println(String.format("PEER:%s:PORT=%s:IP=%s%%", peer.getName(), peer.getPort(), peer.getIp()));
            }

        } catch (IOException | ASN1DecoderFail e) {
            e.printStackTrace();
        }
    }
}