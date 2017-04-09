package com.cse4232.gossip;

import com.cse4232.gossip.helper.Logger;
import com.cse4232.gossip.helper.asn.Peer;
import com.cse4232.gossip.tcp.TCPClient;
import com.cse4232.gossip.udp.UDPClient;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOptsException;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Client {

    private static final int STATUS_TIMER = 5000;

    private JPanel mainPanel;
    private JSplitPane outerSplitPane;
    private JSplitPane innerSplitPane;
    private JPanel gossipPanel;
    private JPanel peerPanel;
    private JPanel peersPanel;
    private JScrollPane peersScrollPane;
    private JList peersList;
    private JPanel innerGossipPanel;
    private JButton gossipButton;
    private JLabel peerLabel;
    private JButton resetButton;
    private JTextField nameTextField;
    private JFormattedTextField ipTextField;
    private JFormattedTextField portTextField;
    private JButton peerButton;
    private JTextField gossipTextField;
    private JLabel gossipLabel;
    private JPanel statusPanel;
    private JLabel connectionMessage;
    private JLabel statusMessage;

    private Logger log;

    public Client(GossipClient client, String host, int port) {
        this.log = Logger.getInstance();

        connectionMessage.setText(String.format("Connected to: %s:%d", host, port));

        Timer timer = new Timer(STATUS_TIMER, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                statusMessage.setText("");
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().equals("synchronize")) {
                    new SwingWorker<Peer[], Void>() {
                        @Override
                        protected Peer[] doInBackground() throws Exception {
                            return client.getPeers();
                        }

                        @Override
                        protected void done() {
                            try {
                                Peer[] peers = get();
                                peersList.setListData(peers);
                                statusMessage.setText("Synchronized Peers");
                                timer.start();

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
                        String gossip = gossipTextField.getText();
                        gossipTextField.setText("");
                        client.sendGossip(gossip);
                        statusMessage.setText("Sent Gossip Message");
                        timer.start();
                    } catch (Exception e) {
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
                        String name = nameTextField.getText();
                        String ip = ipTextField.getText();
                        String port = portTextField.getText();

                        nameTextField.setText("");
                        ipTextField.setText("");
                        portTextField.setText("");

                        client.sendPeer(name, ip, port);
                        statusMessage.setText("Sent Peer Message");
                        timer.start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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

        if (isTcp == isUdp) System.exit(1);

        JFrame frame = new JFrame("Gossip Client");

        try {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            if (isTcp) {

                GossipClient tcpClient = new TCPClient(host, port);
                frame.setContentPane(new Client(tcpClient, host, port).mainPanel);

            } else {

                GossipClient udpClient = new UDPClient(host, port);
                frame.setContentPane(new Client(udpClient, host, port).mainPanel);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}