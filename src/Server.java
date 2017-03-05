import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOptsException;

import java.io.IOException;
import java.util.Arrays;

import static java.lang.System.out;

public class Server {

    private final static boolean APPEND = false;
    private final static boolean DEBUG_MODE = true;

    public static void main(String[] args) {

        int serverPort = 2345;
        String dbConnectionString = "jdbc:sqlite:test.db";
        String logPath = "test.log";

        try (Logger logger = Logger.Initialize(logPath, APPEND, DEBUG_MODE)) {

            logger.log("BEGIN");
            logger.log(String.format("Arguments: %s", Arrays.toString(args)));

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
                logger.log(e);
            }

            logger.log("Connecting To Database Instance...");
            try (DataBaseHandler db = DataBaseHandler.Initialize(dbConnectionString)) {

                logger.log("Recreating Tables...");
                db.recreate();

                Thread tcpServer = new Thread(new TCPserver(serverPort), "TCPserver");
                Thread udpServer = new Thread(new UDPServer(serverPort), "UPDserver");

                logger.log("Starting TCP Server...");
                tcpServer.start();
                logger.log("Starting UPD Server...");
                udpServer.start();

                tcpServer.join();
                udpServer.join();

            } catch (Exception ex) {
                logger.log(ex);
            }

            logger.log("END");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
