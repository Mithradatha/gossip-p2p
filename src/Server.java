import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOptsException;

import java.sql.SQLException;

import static java.lang.System.out;

public class Server {

    public static void main(String[] args) {

        int serverPort = 2345;
        String dbConnectionString = "jdbc:sqlite:test.db";

        GetOpt g = new GetOpt(args, "p:d:");
        int ch = -1;
        try {
            while ((ch = g.getNextOption()) != -1) {
                switch (ch) {
                    case 'p':
                        serverPort = Integer.parseInt(g.getOptionArg());
                        break;
                    case 'd':
                        dbConnectionString = g.getOptionArg();
                        break;
                    default:
                        out.println(ch);
                }
            }
        } catch (GetOptsException e) {
            out.println(e.getClass());
            return;
        }

        try (DataBaseHandler db = DataBaseHandler.getInstance(dbConnectionString);) {

            db.recreate();

            Thread tcpServer = new Thread(new TCPserver(serverPort, db), "TCPserver");
            Thread udpServer = new Thread(new UDPServer(serverPort, db), "UPDserver");

            tcpServer.start();
            udpServer.start();

            tcpServer.join();
            udpServer.join();

        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
