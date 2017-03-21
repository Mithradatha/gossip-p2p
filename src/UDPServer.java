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
                UDPResponder newResponder = new UDPResponder(serverSocket, packet);
                new Thread(newResponder).start();
            }
        } catch (IOException e) {
            logger.log(e);
        }
    }
}
