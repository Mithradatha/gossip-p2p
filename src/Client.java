import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try (
                Socket clientSocket = new Socket(host, port);
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                Scanner sc = new Scanner(System.in);
                //Scanner user_in = new Scanner(System.in);
                ) {

            String welcomeMsg = in.readLine();
            System.out.println(welcomeMsg);

            String stdIn = null;
            String output = null;

            while ((stdIn = sc.nextLine()) != null) {
                out.println(stdIn);
                System.out.println(in.readLine());
            }
        } catch (IOException exp) {
            System.out.println(exp.getMessage());
        }
    }
}
