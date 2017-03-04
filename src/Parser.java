import java.util.List;

class Parser {

    static final String nullOp = "%";
    static final String inSep = ":";
    static final String outSep = "|";
    static final String kv = "=";

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
}
