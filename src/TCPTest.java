import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class TCPTest implements Runnable {

    private String host;
    private int port;
    private List<String> messages;

    public TCPTest(String host, int port, List<String> messages) {
        this.host = host;
        this.port = port;
        this.messages = messages;
    }

    @Override
    public void run() {
        try (
                Socket clientSocket = new Socket(host, port);
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {

            String welcomeMsg = in.readLine();

            Collections.shuffle(messages, new Random());

            for (String message : messages) {
                out.println(message);
                out.flush();
                in.readLine();
            }

        } catch (IOException exp) {
            System.err.println(exp.getMessage());
        }
    }
}
