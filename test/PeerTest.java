import com.cse4232.gossip.helper.asn.Peer;
import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.Decoder;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerTest {

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

            Peer peer = new Peer("Sam", 2345, "163.15.13.123");
            client.getOutputStream().write(peer.encode());

            byte[] msg = new byte[BUFFER_SIZE];
            Decoder decoder;
            int bytesRead;

            do {
                bytesRead = serverInput.read(msg);
                buffer.write(msg);
                decoder = new Decoder(buffer.toByteArray());
                if (bytesRead <= 0) break;
            } while (!decoder.fetchAll(serverInput));

            peer = new Peer();
            peer.decode(decoder);
            System.out.println(String.format("%s: %s:%s", peer.getName(), peer.getIp(), peer.getPort()));

        } catch (IOException | ASN1DecoderFail e) {
            e.printStackTrace();
        }
    }
}
