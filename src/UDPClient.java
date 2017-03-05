
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient {

    private static int PACKET_SIZE = 1024;

    public static void main(String[] args) {

        try (
                DatagramSocket clientSocket = new DatagramSocket();
                Scanner sc = new Scanner(System.in)
        ) {
            //int port = Integer.parseInt(args[1]);
            //InetAddress addr = InetAddress.getByName(args[0]);

            int port = 2345;
            InetAddress addr = InetAddress.getByName("localhost");

            String stdIn;
            while (sc.hasNext()) {
                stdIn = sc.nextLine() + "\n";
                DatagramPacket sendPacket = new DatagramPacket(stdIn.getBytes(), stdIn.length(), addr, port);
                clientSocket.send(sendPacket);
                DatagramPacket receivePacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
                clientSocket.receive(receivePacket);
                String message = new String(receivePacket.getData(), "UTF-8").split("\n")[0];
                System.out.println(message);
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
