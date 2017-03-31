import com.cse4232.gossip.helper.asn.PeersQuery;
import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.Decoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PeersQueryTest {

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

            PeersQuery peersQuery = new PeersQuery();
            client.getOutputStream().write(peersQuery.encode());

            byte[] msg = new byte[BUFFER_SIZE];
            Decoder decoder;
            int bytesRead;

            do {
                bytesRead = serverInput.read(msg);
                buffer.write(msg);
                decoder = new Decoder(buffer.toByteArray());
                if (bytesRead <= 0) break;
            } while (!decoder.fetchAll(serverInput));

            peersQuery = new PeersQuery();
            peersQuery.decode(decoder);
            System.out.println("PEERS?\n");

        } catch (IOException | ASN1DecoderFail e) {
            e.printStackTrace();
        }
    }
}
