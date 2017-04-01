package com.cse4232.gossip.client;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import com.cse4232.gossip.helper.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class TCPClient implements AutoCloseable {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public TCPClient(String host, int port) throws IOException {

        this.clientSocket = new Socket(host, port);
        this.out = out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String welcomeMsg = in.readLine();
    }

    @Override
    public void close() throws IOException {

        out.close();
        in.close();
        clientSocket.close();
    }

    public List<String[]> getPeers() {
        out.write("PEERS?\n");
        out.flush();

        List<String[]> peers = null;

        try {
            peers = Parser.extractSelectedPeers(in.readLine());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return peers;
    }

    public void sendGossip(String message) {
        out.write(message);
        out.flush();

    }

    public void sendPeer(String message) {

        //System.out.println(message);

        out.write(message);
        out.flush();
    }
}
