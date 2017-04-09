package com.cse4232.gossip.newio;

import com.cse4232.gossip.helper.asn.Gossip;
import com.cse4232.gossip.helper.asn.Peer;
import com.cse4232.gossip.helper.asn.PeersAnswer;
import com.cse4232.gossip.helper.asn.PeersQuery;
import com.cse4232.gossip.tcp.TCPClient;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOptsException;
import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASN1_Util;
import net.ddp2p.ASN1.Decoder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Client {

   /* public static final int TCP = 1;
    public static final int UDP = 2;

    private static final int BUFFER_SIZE = 512;

    private JPanel mainPanel;
    private JSplitPane outerSplitPane;
    private JSplitPane innerSplitPane;
    private JPanel gossipPanel;
    private JPanel peerPanel;
    private JPanel peersPanel;
    private JScrollPane peersScrollPane;
    private JList peersList;
    private JPanel innerGossipPanel;
    private JCheckBox gossipCheckBox;
    private JButton gossipButton;
    private JLabel peerLabel;
    private JButton resetButton;
    private JTextField nameTextField;
    private JFormattedTextField ipTextField;
    private JFormattedTextField portTextField;
    private JButton peerButton;
    private JTextField gossipTextField;

    private Socket tcpSocket;
    private DatagramSocket udpSocket;

    public Client(String host, int port, int type) {

        try {
            if (type == TCP) {
                this.tcpSocket = new Socket(host, port);
            } else if (type == UDP) {
                this.udpSocket = new DatagramSocket(new InetSocketAddress(host, port));
            } else throw new Exception("ERROR: TCPClient must either by TCP or UDP type");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }


        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().equals("synchronize")) {
                    new SwingWorker<Peer[], Void>() {
                        @Override
                        protected Peer[] doInBackground() throws Exception {
                            return updatePeers();
                        }

                        @Override
                        protected void done() {
                            try {
                                Peer[] peers = get();
                                peersList.setListData(peers);

                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    }.run();
                }
            }
        });

        gossipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().equals("gossipSubmit")) {
                    try {
                        sendGossip();
                    } catch (IOException | NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        peerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().equals("peerSubmit")) {
                    try {
                        sendPeer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    
    private Peer[] updatePeers() throws IOException, ASN1DecoderFail {

        PeersQuery query = new PeersQuery();
        byte[] out = query.encode();

        PeersAnswer answer = new PeersAnswer();
        byte[] in = new byte[BUFFER_SIZE];

        if (udpSocket != null) {

            DatagramPacket sentPacket = new DatagramPacket(out, out.length, udpSocket.getRemoteSocketAddress());
            udpSocket.send(sentPacket);

            DatagramPacket receivedPacket = new DatagramPacket(in, in.length);
            udpSocket.receive(receivedPacket);

            Decoder decoder = new Decoder(in);
            answer.decode(decoder);

            return answer.getPeers();

        } else {

            OutputStream os = tcpSocket.getOutputStream();
            os.write(out);
            os.flush();

            InputStream is = tcpSocket.getInputStream();

            for (; ;) {

                int msgLen = is.read(in);
                if (msgLen <= 0) break;

                Decoder decoder = new Decoder(in, 0, msgLen);
                if (decoder.fetchAll(is)) {

                    answer.decode(decoder);
                    return answer.getPeers();
                }
            }
        }

        return null;
    }

    private void sendPeer() throws IOException {

        String name = nameTextField.getText();
        String ip = ipTextField.getText();
        String port = portTextField.getText();

        nameTextField.setText("");
        ipTextField.setText("");
        portTextField.setText("");

        Peer peer = new Peer(name, Integer.parseInt(port), ip);
        byte[] out = peer.encode();

        if (udpSocket != null) {

            DatagramPacket sentPacket = new DatagramPacket(out, out.length, udpSocket.getRemoteSocketAddress());
            udpSocket.send(sentPacket);

        } else {

            OutputStream os = tcpSocket.getOutputStream();
            os.write(out);
            os.flush();
        }
    }

    private void sendGossip() throws NoSuchAlgorithmException, IOException {

        String fullMessage = "";
        String message = gossipTextField.getText();
        gossipTextField.setText("");


        if (gossipCheckBox.isSelected()) {

            Calendar timestamp = ASN1_Util.CalendargetInstance();
            fullMessage += ASN1_Util.getStringDate(timestamp);
            fullMessage += ":";

            fullMessage += message;

            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(fullMessage.getBytes());
            String hash = Base64.getEncoder().encodeToString(digest);

            Gossip gossip = new Gossip(hash, timestamp, message);
            byte[] out = gossip.encode();

            if (udpSocket != null) {

                DatagramPacket sentPacket = new DatagramPacket(out, out.length, udpSocket.getRemoteSocketAddress());
                udpSocket.send(sentPacket);

            } else {

                OutputStream os = tcpSocket.getOutputStream();
                os.write(out);
                os.flush();
            }
        }
    }*/

    public static void main(String[] args) {

        String host = "";
        int port = 0;

        boolean isTcp = false;

        GetOpt g = new GetOpt(args, "s:p:TU");
        int ch = -1;
        try {
            while ((ch = g.getNextOption()) != -1) {
                switch (ch) {
                    case 's':
                        host = g.getOptionArg();
                        break;
                    case 'p':
                        port = Integer.parseInt(g.getOptionArg());
                        break;
                    case 'T':
                        isTcp = true;
                        break;
                    case 'U':
                        //frame.setContentPane(new Client(host, port, UDP).mainPanel);
                        break;
                    default:
                        g.printOptions();
                }
            }
        } catch (GetOptsException e) {
            e.printStackTrace();
            System.exit(1);
        }

        if (isTcp) {
            try {
                TCPClient tcpClient = new TCPClient(host, port);
                //tcpClient.run();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /*private void updatePeersTCP() {
        List<String[]> peers = tcpClient.getPeers();
        List<String> str = new ArrayList<>();
        peers.forEach((peer) -> str.add(peer[0] + " - " + peer[2] + ":" + peer[1]));
        peersList.setListData(str.toArray());
    }

    private void updatePeersUDP() {
        List<String[]> peers = udpClient.getPeers();
        List<String> str = new ArrayList<>();
        peers.forEach((peer) -> str.add(peer[0] + " - " + peer[2] + ":" + peer[1]));
        peersList.setListData(str.toArray());
    }

    private void sendGossipTCP() {
        try {

            String fullMessage = "";
            String message = gossipTextField.getText();
            gossipTextField.setText("");
            if (gossipCheckBox.isSelected()) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSSz")
                        .format(new Timestamp(System.currentTimeMillis()));
                fullMessage = timestamp + ":";
            }
            fullMessage += message;

            //System.out.println(fullMessage);

            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(fullMessage.getBytes());
            String hash = Base64.getEncoder().encodeToString(digest);

            //System.out.println(hash);

            tcpClient.sendGossip(MessageFormat.format("GOSSIP:{0}:{1}%\n", hash, fullMessage));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void sendGossipUDP() {
        try {

            String fullMessage = "";
            String message = gossipTextField.getText();
            gossipTextField.setText("");
            if (gossipCheckBox.isSelected()) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSSz")
                        .format(new Timestamp(System.currentTimeMillis()));
                fullMessage = timestamp + ":";
            }
            fullMessage += message;

            //System.out.println(fullMessage);

            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(fullMessage.getBytes());
            String hash = Base64.getEncoder().encodeToString(digest);

            //System.out.println(hash);

            udpClient.sendGossip(MessageFormat.format("GOSSIP:{0}:{1}%\n", hash, fullMessage));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void sendPeerTCP() {
        String name = nameTextField.getText();
        String ip = ipTextField.getText();
        String port = portTextField.getText();

        nameTextField.setText("");
        ipTextField.setText("");
        portTextField.setText("");

        tcpClient.sendPeer(MessageFormat.format("PEER:{0}:PORT={1}:IP={2}%\n", name, port, ip));
    }

    private void sendPeerUDP() {
        String name = nameTextField.getText();
        String ip = ipTextField.getText();
        String port = portTextField.getText();

        nameTextField.setText("");
        ipTextField.setText("");
        portTextField.setText("");

        udpClient.sendPeer(MessageFormat.format("PEER:{0}:PORT={1}:IP={2}%\n", name, port, ip));
    }*/
}
