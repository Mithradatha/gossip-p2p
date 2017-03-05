import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

public class Logger implements AutoCloseable {

    private static Logger instance;

    static Logger Initialize(String path, boolean append) throws IOException {
        if (instance == null) {
            instance = new Logger(path, append);
        }
        return instance;
    }

    static Logger getInstance() { return instance; }

    private FileOutputStream fileOutputStream;

    private Logger(String path, boolean append) throws IOException {
        File logFile = new File(path);
        boolean isNew = logFile.createNewFile();
        this.fileOutputStream = new FileOutputStream(logFile, append);
    }

    @Override
    public void close() throws IOException {
        fileOutputStream.close();
    }

    void log(Exception ex) {
        try {
            String string = String.format("%s: !ERROR! %s\n",
                    (new Timestamp(System.currentTimeMillis())).toString(),
                    ex.getMessage());
            fileOutputStream.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void log(String str) {
        try {
            String string = String.format("%s: %s\n",
                    (new Timestamp(System.currentTimeMillis())).toString(),
                    str);
            fileOutputStream.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

