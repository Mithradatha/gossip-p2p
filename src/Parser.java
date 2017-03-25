import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Parser {

    final static String nullOp = "%";
    final static String inSep = ":";
    final static String lineSep = "\\|";
    final static String kv = "=";

    final static String GREETINGS = "HELLO";
    final static String SERVER = "server";
    final static String CLIENT = "client";

    final static String UDP = "udp";
    final static String TCP = "tcp";

    private final static String RES_UNKNOWN = "BAD UNKNOWN COMMAND";
    private final static String RES_INVALID = "BAD INVALID FORMAT";
    private final static String RES_SUCCESS = "OK";

    private static String ParseSelectedPeers(List<String[]> peers) {
        StringBuilder peerResponse = new StringBuilder();
        int count = peers.size();
        peerResponse.append(String.format("PEERS|%s|", count));
        for (String[] peer : peers) {
            String name = peer[0];
            String port = peer[1];
            String ip = peer[2];
            peerResponse.append(String.format("%s:PORT=%s:IP=%s|", name, port, ip));
        }
        peerResponse.append(nullOp);
        return peerResponse.toString();
    }

    static String ParseAndExecuteCommand(String request, DataBaseHandler db) throws SQLException, IOException {

        String[] input = request.split(Parser.inSep);
        String command = input[0];

        String output = "";

        switch (command) {
            case "GOSSIP": {
                if (input.length != 4) {
                    return RES_INVALID;
                }

                String sha = input[1];
                String dt = input[2];
                String message = input[3].substring(0, input[3].indexOf(Parser.nullOp));

                if (db.exists(sha)) {
                    output = "DISCARDED";
                } else {
                    db.insertGossip(sha, dt, message);
                    Broadcaster.getInstance().broadcast(request, db.selectPeers());
                    output = request;
                }
                break;
            }
            case "PEER": {
                if (input.length != 4) {
                    return RES_INVALID;
                }

                String name = input[1];
                String port = input[2].split(Parser.kv)[1];
                String ip = input[3].substring(0, input[3].indexOf(Parser.nullOp)).split(Parser.kv)[1];

                db.insertPeer(name, port, ip);
                output = RES_SUCCESS;
                break;
            }
            case "PEERS?": {
                if (input.length != 1) {
                    return RES_INVALID;
                }

                output = Parser.ParseSelectedPeers(db.selectPeers());
                break;
            }
            default:
                output = RES_UNKNOWN;
                break;
        }
        return output;
    }

    public static List<String[]> extractSelectedPeers(String message) {
        List<String[]> result = new ArrayList<>();
        //System.out.println(message);
        String[] arr = message.split(lineSep);
        for (int i = 2; i < arr.length - 1; i++) {
            String[] str = arr[i].split(inSep);
            str[1] = str[1].replace("PORT=", "");
            str[2] = str[2].replace("IP=", "");
            result.add(str);
        }
        assert arr[1].equals(Integer.toString(result.size()));

        return result;
    }
}