package edu.cse4232.gossip.context;

/**
 * Builder for Server Context
 */
public class ContextBuilder {

    private DataBaseHandler dataBaseHandler;
    private Broadcaster broadcaster;
    private Logger logger;

    public ContextBuilder setDataBaseHandler(DataBaseHandler dataBaseHandler) {
        this.dataBaseHandler = dataBaseHandler;
        return this;
    }

    public ContextBuilder setBroadcaster(Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
        return this;
    }

    public ContextBuilder setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    /**
     * @return New Context
     */
    public Context createContext() {
        return new Context(dataBaseHandler, broadcaster, logger);
    }
}
