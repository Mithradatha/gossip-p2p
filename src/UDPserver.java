import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

class UDPServer implements Runnable {

    private final static int PACKET_SIZE = 1024;

    private int port;
    private DataBaseHandler db;

    UDPServer(int port, DataBaseHandler db) {
        this.port = port;
        this.db = db;
    }

    @Override
    public void run() {
        try (DatagramSocket serverSocket = new DatagramSocket(port)) {
            for(;;) {
                DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
                serverSocket.receive(packet);

                System.out.println(packet.getAddress() + " " + packet.getPort() + " " + Arrays.toString(packet.getData()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
