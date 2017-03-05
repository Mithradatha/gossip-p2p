import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Created by Nemahs on 3/5/2017.
 */
public class UDPClient {
    private static int PACKET_SIZE = 1024;

    public static void main(String[] args)
    {
        try {
            int port = Integer.parseInt(args[1]);
            InetAddress addr = InetAddress.getByName(args[0]);
            DatagramSocket sock = new DatagramSocket(port, addr);
            Scanner cin = new Scanner(System.in);


            while (cin.hasNext())
            {
                String command = cin.nextLine();
                DatagramPacket pack = new DatagramPacket(command.getBytes(), command.length(), addr, port);
                sock.send(pack);

                DatagramPacket message = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
                sock.receive(message);
                System.out.println(new String(pack.getData()).split("\n")[0]);
            }


        }
        catch (java.net.SocketException e)
        {
            System.err.println(e.getMessage());
        }
        catch (java.net.UnknownHostException e)
        {
            System.err.println(e.getMessage());
        }
        catch (java.io.IOException e)
        {
            System.err.println(e.getMessage());
        }
    }
}
