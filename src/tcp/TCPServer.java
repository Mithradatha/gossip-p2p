package com.cse4232.gossip.tcp;

import com.cse4232.gossip.newio.ClientHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer implements Runnable, AutoCloseable {

    private ServerSocket tcpServer;

    public TCPServer(int port) throws IOException {
        this.tcpServer = new ServerSocket(port);
    }

    @Override
    public void close() throws Exception {
        if (tcpServer != null) tcpServer.close();
    }

    @Override
    public void run() {

        for (; ; ) {

            try {

                Socket client = tcpServer.accept();
                new Thread(new TCPResponder(client)).start();

            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
