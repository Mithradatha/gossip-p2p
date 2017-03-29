package com.cse4232.gossip.client;

import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOptsException;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Client {

    TCPClient tcpClient;
    UDPClient udpClient;

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


    private Client(String host, int port, boolean isTcp) {

        try {
            if (isTcp) {
                tcpClient = new TCPClient(host, port);
            } else {
                udpClient = new UDPClient(host, port);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().equals("synchronize")) {
                    if (isTcp) updatePeersTCP();
                    else updatePeersUDP();
                }
            }
        });

        gossipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().equals("gossipSubmit")) {
                    if (isTcp) sendGossipTCP();
                    else sendGossipUDP();
                }
            }
        });

        peerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().equals("peerSubmit")) {
                    if (isTcp) sendPeerTCP();
                    else sendPeerUDP();
                }
            }
        });
    }

    public static void main(String[] args) {

        String host = "";
        int port = 0;

        boolean isTcp = false;
        boolean isUdp = false;

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
                        isUdp = true;
                        break;
                    default:
                        g.printOptions();
                }
            }
        } catch (GetOptsException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println(String.format("TCP: %s", isTcp));
        System.out.println(String.format("UDP: %s", isUdp));

        if (!isTcp && !isUdp) {
            System.exit(1);
        }

        JFrame frame = new JFrame("Gossip Client");
        frame.setContentPane(new Client(host, port, isTcp).mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void updatePeersTCP() {
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
    }
}
