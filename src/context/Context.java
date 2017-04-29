package edu.cse4232.gossip.context;

/**
 * Holder for Server Assets
 */
public class Context {

    private final DataBaseHandler dataBaseHandler;
    private final Broadcaster broadcaster;
    private final Logger logger;

    Context(DataBaseHandler dataBaseHandler, Broadcaster broadcaster, Logger logger) {
        this.dataBaseHandler = dataBaseHandler;
        this.broadcaster = broadcaster;
        this.logger = logger;
    }

    public DataBaseHandler getDataBaseHandler() throws ContextException {
        if (dataBaseHandler == null) {
            throw new ContextException("DatabaseHandler not initialized");
        }
        return dataBaseHandler;
    }

    public Broadcaster getBroadcaster() throws ContextException {
        if (broadcaster == null) {
            throw new ContextException("Broadcaseter not initialized");
        }
        return broadcaster;
    }

    public Logger getLogger() throws ContextException {
        if (logger == null) {
            throw new ContextException("Logger not initialized");
        }
        return logger;
    }
}
