import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.sql.SQLException;

class UDPServer implements Runnable {

    private final static int PACKET_SIZE = 1024;

    private int port;
    private DataBaseHandler db;
    private Logger logger;

    UDPServer(int port) {
        this.port = port;
        this.db = DataBaseHandler.getInstance();
        this.logger = Logger.getInstance();
    }

    @Override
    public void run() {
        try (DatagramSocket serverSocket = new DatagramSocket(port)) {
            for (; ; ) {
                DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
                serverSocket.receive(packet);

                String message = new String(packet.getData(), "UTF-8").split("\n")[0];
                logger.log(Parser.UDP, Parser.CLIENT, message);

                String output = Parser.ParseAndExecuteCommand(message, db) + "\n";
                logger.log(Parser.UDP, Parser.SERVER, output.substring(0, output.length()-1));

                DatagramPacket response = new DatagramPacket(output.getBytes(), output.length(), packet.getAddress(), packet.getPort());
                serverSocket.send(response);
            }
        } catch (IOException | SQLException e) {
            logger.log(e);
        }
    }
}
