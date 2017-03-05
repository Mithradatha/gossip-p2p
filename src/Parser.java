import java.util.List;

class Parser {

    final static String nullOp = "%";
    final static String inSep = ":";
    final static String outSep = "|";
    final static String kv = "=";

    static String ParseSelectedPeers(List<String[]> peers) {
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

    static String parseAndExecuteCommand(String request, DataBaseHandler db) {
        try {
            String[] input = request.split(Parser.inSep);
            String command = input[0];
            System.out.println(command);
            String output;
            switch (command) {
                case "GOSSIP": {
                    String sha = input[1];
                    String dt = input[2];
                    String message = input[3].substring(0, input[3].indexOf(Parser.nullOp));
                    if (db.exists(sha)) {
                        output = "DISCARDED";
                    } else {
                        System.out.println(sha);
                        System.out.println(dt);
                        System.out.println(message);
                        db.insertGossip(sha, dt, message);
                        output = "Inserted";
                    }
                    break;
                }
                case "PEER": {
                    String name = input[1];
                    String port = input[2].split(Parser.kv)[1];
                    String ip = input[3].substring(0, input[3].indexOf(Parser.nullOp)).split(Parser.kv)[1];
                    db.insertPeer(name, port, ip);
                    output = "Inserted";
                    break;
                }
                case "PEERS?": {
                    String peerResponse = Parser.ParseSelectedPeers(db.selectPeers());
                    output = peerResponse;
                    break;
                }
                default:
                    output = "Unknown Command: " + command;
                    break;
            }
            return output;
        } catch (java.sql.SQLException e) {
            System.out.println(e.getMessage());
            return "Database Error";
        }
    }
}