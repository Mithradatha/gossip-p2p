import java.sql.SQLException;
import java.util.List;

public class Server {

    public static void main(String[] args) {

        //Class.forName("org.sqlite.JDBC");

        //int server_port = Integer.parseInt(args[0]);
        //String server_db = args[1];

        int serverPort = 2345;
        String dbConnectionString = "jdbc:sqlite:test.db";


        try (DataBaseHandler db = DataBaseHandler.getInstance(dbConnectionString);) {

            db.recreate();

            Thread tcpServer = new Thread(new TCPserver(serverPort, db), "TCPserver");
            //Thread udpServer = new Thread(new UDPserver(serverPort, db), "UPDserver");

            tcpServer.start();
            //udpServer.start();

            tcpServer.join();
            //udpServer.join();

        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
