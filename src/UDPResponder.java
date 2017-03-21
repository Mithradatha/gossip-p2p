import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.sql.SQLException;

/**
 * Created by Nemahs on 3/21/2017.
 */
public class UDPResponder implements Runnable {
    private DatagramPacket packet;
    private DatagramSocket socket;
    private Logger logger;
    private DataBaseHandler db;

    public UDPResponder(DatagramSocket sock, DatagramPacket pack)
    {
        socket = sock;
        packet = pack;
        logger = Logger.getInstance();
        db = DataBaseHandler.getInstance();
    }

    @Override
    public void run() {
        try {
            String message = new String(packet.getData(), "UTF-8").split("\n")[0];
            logger.log(Parser.UDP, Parser.CLIENT, message);

            String output = Parser.ParseAndExecuteCommand(message, db) + "\n";
            logger.log(Parser.UDP, Parser.SERVER, output.substring(0, output.length() - 1));

            DatagramPacket response = new DatagramPacket(output.getBytes(), output.length(), packet.getAddress(), packet.getPort());
            socket.send(response);
        } catch (IOException | SQLException ex)
        {
            logger.log(ex);
        }
    }
}
