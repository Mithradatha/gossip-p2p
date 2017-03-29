import java.util.Arrays;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int clients = Integer.parseInt(args[2]);

        List<String> messages = Arrays.asList("PEERS?",
                "GOSSIP:mBHL7IKilvdcOFKR03ASvBNX//ypQkTRUvilYmB1/OY=:2017-01-09-16-18-20-001Z:Tom eats Jerry%",
                "PEER:John:PORT=2356:IP=163.118.239.68%");

        for (int i = 0; i < clients; i++) {
            new Thread(new TCPTest(host, port, messages)).start();
        }
    }
}
