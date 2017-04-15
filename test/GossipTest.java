package test;

import com.cse4232.gossip.helper.asn.Gossip;
import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.Decoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

class GossipTest {

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

            Gossip gossip = new Gossip("hash", Calendar.getInstance(), "gossiping");
            client.getOutputStream().write(gossip.encode());

            byte[] msg = new byte[BUFFER_SIZE];
            Decoder decoder;
            int bytesRead;

            do {
                bytesRead = serverInput.read(msg);
                buffer.write(msg);
                decoder = new Decoder(buffer.toByteArray());
                if (bytesRead <= 0) break;
            } while (!decoder.fetchAll(serverInput));

            gossip = new Gossip();
            gossip.decode(decoder);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSSz");
            System.out.println(String.format("GOSSIP:%s:%s:%s%%", gossip.getSha256hash(),
                    format.format(gossip.getTimestamp().getTime()), gossip.getMessage()));

        } catch (IOException | ASN1DecoderFail e) {
            e.printStackTrace();
        }
    }
}
