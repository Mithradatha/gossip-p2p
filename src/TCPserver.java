import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

class TCPserver implements Runnable {

    private int port;
    private DataBaseHandler db;
    private Logger logger;

    TCPserver(int port) {
        this.port = port;
        this.db = DataBaseHandler.getInstance();
        this.logger = Logger.getInstance();
    }

    @Override
    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            for (; ; ) {
                try (
                        Socket clientSocket = serverSocket.accept();
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                ) {
                    // Parser.GREETINGS
                    out.println(Parser.GREETINGS);
                    out.flush();

                    logger.log(Parser.TCP, Parser.SERVER, Parser.GREETINGS);

                    String request;
                    while ((request = in.readLine()) != null) {
                        logger.log(Parser.TCP, Parser.CLIENT, request);

                        String response = Parser.ParseAndExecuteCommand(request, db);
                        out.println(response);
                        out.flush();

                        logger.log(Parser.TCP, Parser.SERVER, response);
                    }
                } catch (IOException | SQLException ex) {
                    logger.log(ex);
                }
            }
        } catch (IOException e) {
            logger.log(e);
        }
    }
}
