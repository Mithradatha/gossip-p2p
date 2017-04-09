package com.cse4232.gossip;

import com.cse4232.gossip.helper.Logger;
import com.cse4232.gossip.helper.asn.Peer;
import com.cse4232.gossip.tcp.TCPClient;
import com.cse4232.gossip.udp.UDPClient;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Client implements Runnable {

    private static final int STATUS_TIMER = 5000;
    private static final String TITLE_ICON = "img/title.png";

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
    private GossipClient client;

    public Client(GossipClient client) {
        this.client = client;
        this.log = Logger.getInstance();

        String host = client.getHost();
        int port = client.getPort();
        String type = client.getType();

        connectionMessage.setText(String.format("Connected to: [%s] %s:%d", type, host, port));

        Timer timer = new Timer(STATUS_TIMER, actionEvent -> statusMessage.setText(""));

        resetButton.addActionListener(actionEvent -> {
            if (actionEvent.getActionCommand().equals("synchronize")) {
                new SwingWorker<Peer[], Void>() {

                    @Override
                    protected Peer[] doInBackground() throws Exception { return client.getPeers(); }

                    @Override
                    protected void done() {

                        try {

                            Peer[] peers = get();
                            peersList.setListData(peers);
                            statusMessage.setText("Synchronized Peers");
                            timer.start();

                        } catch (Exception e) {
                            shutdown(e);
                        }
                    }
                }.run();
            }
        });

        gossipButton.addActionListener(actionEvent -> {
            if (actionEvent.getActionCommand().equals("gossipSubmit")) {
                try {
                    String gossip = gossipTextField.getText();
                    gossipTextField.setText("");
                    client.sendGossip(gossip);
                    statusMessage.setText("Sent Gossip Message");
                    timer.start();
                } catch (Exception e) {
                    shutdown(e);
                }
            }
        });

        peerButton.addActionListener(actionEvent -> {
            if (actionEvent.getActionCommand().equals("peerSubmit")) {
                try {
                    String name = nameTextField.getText();
                    String ip = ipTextField.getText();
                    String port1 = portTextField.getText();

                    nameTextField.setText("");
                    ipTextField.setText("");
                    portTextField.setText("");

                    client.sendPeer(name, ip, port1);
                    statusMessage.setText("Sent Peer Message");
                    timer.start();

                } catch (Exception e) {
                    shutdown(e);
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

            GossipClient gossipClient = isTcp? new TCPClient(host, port) : new UDPClient(host, port);
            new Client(gossipClient).run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            JFrame frame = new JFrame("Gossip Client");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            frame.setContentPane(this.mainPanel);
            frame.setIconImage(new ImageIcon(TITLE_ICON).getImage());
            //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    super.windowClosing(windowEvent);
                    shutdown(new Exception(""));
                }
            });
            frame.pack();
            frame.setVisible(true);
        } catch (Exception e) {
            log.log(e);
        }
    }

    private void shutdown(Exception e) {
        log.log(Logger.NOP, Logger.CLIENT, Logger.WARN, String.format("Shutting Down... %s", e.getLocalizedMessage()));
        client.close();
        System.exit(1);
    }
}