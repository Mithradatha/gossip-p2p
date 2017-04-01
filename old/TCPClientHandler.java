package com.cse4232.gossip.server;

import com.cse4232.gossip.helper.DataBaseHandler;
import com.cse4232.gossip.helper.Logger;
import com.cse4232.gossip.helper.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;

/**
 * Created by Nemahs on 3/21/2017.
 * Handles an individual client in its own thread for better scaling
 */
public class TCPClientHandler implements Runnable {

    private Socket clientSocket;
    private Logger logger;
    private DataBaseHandler db;


    TCPClientHandler(Socket sock)
    {
        clientSocket = sock;
        logger = Logger.getInstance();
        db = DataBaseHandler.getInstance();
    }

    @Override
    public void run() {
        try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {
            // Parser.GREETINGS
            out.println(Parser.GREETINGS);
            out.flush();

            logger.log(Parser.TCP, Parser.SERVER, Parser.GREETINGS);

            String request;
            while ((request = in.readLine()) != null) {
                logger.log(Parser.TCP, Parser.CLIENT, request);

                String response = Parser.ParseAndExecuteCommand(request, db);
                out.println(response);
                out.flush();

                logger.log(Parser.TCP, Parser.SERVER, response);
            }
        }
        catch(IOException | SQLException ex)
        {
            logger.log(ex);
        }
    }
}
