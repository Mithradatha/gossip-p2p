
import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Scanner;

public class UDPClient implements AutoCloseable {

    private static int PACKET_SIZE = 1024;

    private DatagramSocket clientSocket;
    private InetAddress addr;
    private int port;

    public UDPClient(String host, int port) throws Exception {
        this.clientSocket = new DatagramSocket();
        this.addr = InetAddress.getByName(host);
        this.port = port;
    }

    public List<String[]> getPeers() {
        String message = "PEERS?\n";
        DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), addr, port);
        List<String[]> peers = null;
        try {
            clientSocket.send(sendPacket);
            DatagramPacket receivePacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
            clientSocket.receive(receivePacket);
            String response = new String(receivePacket.getData(), "UTF-8").split("\n")[0];
            peers = Parser.extractSelectedPeers(response);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return peers;
    }

    public void sendGossip(String message) {
        DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), addr, port);
        try {
            clientSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPeer(String message) {
        DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), addr, port);
        try {
            clientSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        clientSocket.close();
    }
}
