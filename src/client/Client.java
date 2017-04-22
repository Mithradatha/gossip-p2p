package edu.cse4232.gossip.client;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;
import edu.cse4232.gossip.asn1.Peer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

class Client implements Runnable {

    private static final int STATUS_TIMER = 5000;
    private static final String TITLE_ICON = "img/title.png";

    private JPanel mainPanel;
    private JList peersList;
    private JButton gossipButton;
    private JButton resetButton;
    private JTextField nameTextField;
    private JFormattedTextField ipTextField;
    private JFormattedTextField portTextField;
    private JButton peerButton;
    private JTextField gossipTextField;
    private JLabel connectionMessage;
    private JLabel statusMessage;
    private JSplitPane outerSplitPane;
    private JPanel statusPanel;
    private JSplitPane innerSplitPane;
    private JPanel peersPanel;
    private JLabel gossipLabel;
    private JPanel gossipPanel;
    private JPanel innerGossipPanel;
    private JPanel peerPanel;
    private JLabel peerLabel;
    private JScrollPane peersScrollPane;

    // private final Logger log;
    private final GossipClient client;

    private Client(GossipClient client) {
        this.client = client;
        // this.log = Logger.getInstance();

        String host = client.getHost();
        int port = client.getPort();
        String type = client.getType();

        connectionMessage.setText(String.format("Connected to: [%s] %s:%d", type, host, port));

        Timer timer = new Timer(STATUS_TIMER, actionEvent -> statusMessage.setText(""));

        resetButton.addActionListener(actionEvent -> {
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
                            String[] fancyPeers = Arrays.stream(peers).map(Peer::toFancyString).toArray(String[]::new);
                            //noinspection unchecked
                            peersList.setListData(fancyPeers);
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
                    String gossip = gossipTextField.getText().trim();
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
                    String name = nameTextField.getText().trim();
                    String ip = ipTextField.getText().trim();
                    String port1 = portTextField.getText().trim();

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
        boolean isInteractive = false;

        GetOpt g = new GetOpt(args, "s:p:ITU");
        int ch;
        try {
            while ((ch = g.getNextOption()) != -1) {
                switch (ch) {
                    case 's':
                        host = g.getOptionArg();
                        break;
                    case 'p':
                        port = Integer.parseInt(g.getOptionArg());
                        break;
                    case 'I':
                        isInteractive = true;
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

            if (isTcp == isUdp) throw new Exception("one or the other...");

            int type = (isTcp) ? GossipClient.TCP : GossipClient.UDP;

            GossipClient gossipClient = GossipClientFactory.makeClient(host, port, type);

            if (isInteractive) new Client(gossipClient).run();
            else {

                final String[] menu = new String[]{"1. Send Gossip", "2. Send Peer", "3. Get Peers", "4. Exit"};
                final String symbol = ">> ";

                System.out.println();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

                    String input;
                    do {
                        Arrays.stream(menu).forEach(System.out::println);
                        System.out.println();
                        System.out.print(symbol);
                        input = reader.readLine();
                        System.out.println();

                        switch (input) {
                            case "1":
                                System.out.print("Message: ");
                                String message = reader.readLine();
                                System.out.println();
                                gossipClient.sendGossip(message);
                                break;
                            case "2":
                                System.out.print("Name: ");
                                String name = reader.readLine();
                                System.out.println();
                                System.out.print("IP: ");
                                String ip = reader.readLine();
                                System.out.println();
                                System.out.print("Port: ");
                                String portnum = reader.readLine();
                                System.out.println();
                                gossipClient.sendPeer(name, ip, portnum);
                                break;
                            case "3":
                                Peer[] peers = gossipClient.getPeers();
                                Arrays.stream(peers).forEach(System.out::println);
                                System.out.println();
                                break;
                            case "4":
                                break;
                            default:
                                System.out.println("Enter a number in the range [1, 4]");
                                System.out.println();
                        }

                    } while (!input.equals("4"));
                }
            }

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
            // log.log(e);
        }
    }

    private void shutdown(Exception ignored) {
        // log.log(Logger.NOP, Logger.CLIENT, Logger.WARN, String.format("Shutting Down... %s", e.getLocalizedMessage()));
        client.close();
        System.exit(1);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        mainPanel.setFont(UIManager.getFont("Panel.font"));
        mainPanel.setMaximumSize(new Dimension(1366, 768));
        mainPanel.setMinimumSize(new Dimension(600, 500));
        mainPanel.setOpaque(true);
        mainPanel.setPreferredSize(new Dimension(1024, 768));
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), null));
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(668);
        splitPane1.setDividerSize(20);
        splitPane1.setOrientation(0);
        mainPanel.add(splitPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        outerSplitPane = new JSplitPane();
        outerSplitPane.setDividerLocation(512);
        outerSplitPane.setDividerSize(20);
        outerSplitPane.setDoubleBuffered(false);
        outerSplitPane.setEnabled(false);
        outerSplitPane.setFocusTraversalPolicyProvider(false);
        outerSplitPane.setFocusable(true);
        outerSplitPane.setOpaque(true);
        splitPane1.setLeftComponent(outerSplitPane);
        innerSplitPane = new JSplitPane();
        innerSplitPane.setDividerLocation(250);
        innerSplitPane.setDividerSize(20);
        innerSplitPane.setEnabled(false);
        innerSplitPane.setOpaque(true);
        innerSplitPane.setOrientation(0);
        outerSplitPane.setLeftComponent(innerSplitPane);
        gossipPanel = new JPanel();
        gossipPanel.setLayout(new GridLayoutManager(3, 1, new Insets(20, 5, 15, 5), -1, -1));
        gossipPanel.setFont(UIManager.getFont("Panel.font"));
        innerSplitPane.setLeftComponent(gossipPanel);
        gossipLabel = new JLabel();
        gossipLabel.setFocusable(false);
        gossipLabel.setFont(UIManager.getFont("Label.font"));
        gossipLabel.setText("Gossip Message");
        gossipPanel.add(gossipLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        innerGossipPanel = new JPanel();
        innerGossipPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        innerGossipPanel.setFont(UIManager.getFont("Panel.font"));
        gossipPanel.add(innerGossipPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(283, 34), null, 0, false));
        gossipTextField = new JTextField();
        gossipTextField.setColumns(30);
        gossipTextField.setOpaque(false);
        gossipTextField.setPreferredSize(new Dimension(408, 60));
        innerGossipPanel.add(gossipTextField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        gossipButton = new JButton();
        gossipButton.setActionCommand("gossipSubmit");
        gossipButton.setHorizontalTextPosition(0);
        gossipButton.setMargin(new Insets(10, 30, 10, 30));
        gossipButton.setText("Submit");
        gossipPanel.add(gossipButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(300, -1), new Dimension(300, -1), null, 0, false));
        peerPanel = new JPanel();
        peerPanel.setLayout(new GridLayoutManager(3, 1, new Insets(20, 10, 15, 15), -1, -1));
        peerPanel.setFont(UIManager.getFont("Panel.font"));
        innerSplitPane.setRightComponent(peerPanel);
        peerLabel = new JLabel();
        peerLabel.setText("Peer Message");
        peerPanel.add(peerLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 2, new Insets(5, 5, 5, 5), -1, -1));
        panel1.setFont(UIManager.getFont("Panel.font"));
        peerPanel.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Name");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("IP");
        panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Port");
        panel1.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameTextField = new JTextField();
        panel1.add(nameTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        ipTextField = new JFormattedTextField();
        panel1.add(ipTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        portTextField = new JFormattedTextField();
        panel1.add(portTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        peerButton = new JButton();
        peerButton.setActionCommand("peerSubmit");
        peerButton.setMargin(new Insets(10, 30, 10, 30));
        peerButton.setText("Submit");
        peerPanel.add(peerButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(300, -1), new Dimension(300, -1), null, 0, false));
        peersPanel = new JPanel();
        peersPanel.setLayout(new GridLayoutManager(2, 1, new Insets(20, 0, 0, 5), -1, 20));
        peersPanel.setFont(UIManager.getFont("Panel.font"));
        outerSplitPane.setRightComponent(peersPanel);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 5));
        peersPanel.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Known Peers");
        panel2.add(label4);
        resetButton = new JButton();
        resetButton.setActionCommand("synchronize");
        resetButton.setHorizontalTextPosition(10);
        resetButton.setIcon(new ImageIcon(getClass().getResource("/reset.png")));
        resetButton.setIconTextGap(10);
        resetButton.setInheritsPopupMenu(false);
        resetButton.setLabel("");
        resetButton.setMargin(new Insets(5, 25, 5, 25));
        resetButton.setText("");
        panel2.add(resetButton);
        peersScrollPane = new JScrollPane();
        peersPanel.add(peersScrollPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        peersList = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        peersList.setModel(defaultListModel1);
        peersScrollPane.setViewportView(peersList);
        statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayoutManager(1, 2, new Insets(10, 10, 10, 10), -1, -1));
        splitPane1.setRightComponent(statusPanel);
        connectionMessage = new JLabel();
        connectionMessage.setHorizontalAlignment(0);
        connectionMessage.setHorizontalTextPosition(0);
        connectionMessage.setText("");
        statusPanel.add(connectionMessage, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        statusMessage = new JLabel();
        statusMessage.setFont(new Font(statusMessage.getFont().getName(), Font.BOLD, statusMessage.getFont().getSize()));
        statusMessage.setForeground(new Color(-1552849));
        statusMessage.setText("");
        statusPanel.add(statusMessage, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}