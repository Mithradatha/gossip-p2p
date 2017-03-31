package com.cse4232.gossip.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

public class Logger implements AutoCloseable {

    private static Logger instance;

    public static Logger Initialize(String path, boolean append, boolean debug) throws IOException {
        if (instance == null) {
            instance = new Logger(path, append, debug);
        }
        return instance;
    }

    public static Logger getInstance() {
        return instance;
    }

    private FileOutputStream fileOutputStream;
    private boolean debugMode;

    private Logger(String path, boolean append, boolean debug) throws IOException {

        this.debugMode = debug;
        if (!debug) {
            return;
        }

        File logFile = new File(path);
        boolean isNew = logFile.createNewFile();

        this.fileOutputStream = new FileOutputStream(logFile, append);
    }

    @Override
    public void close() throws IOException {
        if (fileOutputStream != null) {
            fileOutputStream.close();
        }
    }

    public synchronized void log(Exception ex) {
        if (!debugMode) {
            return;
        }
        try {
            String string = String.format("%s: !ERROR! %s\n",
                    (new Timestamp(System.currentTimeMillis())).toString(),
                    ex.getMessage());
            fileOutputStream.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void log(String str) {
        if (!debugMode) {
            return;
        }
        try {
            String string = String.format("%s: %s\n",
                    (new Timestamp(System.currentTimeMillis())).toString(),
                    str);
            fileOutputStream.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void log(String type, String side, String str) {
        if (!debugMode) {
            return;
        }
        try {
            String string = String.format("%s: [%s][%s] %s\n",
                    (new Timestamp(System.currentTimeMillis())).toString(),
                    type,
                    side,
                    str);
            fileOutputStream.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

