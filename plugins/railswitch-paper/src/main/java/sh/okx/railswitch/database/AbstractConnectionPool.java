package sh.okx.railswitch.database;

public abstract class AbstractConnectionPool implements ConnectionPool {
    protected final String host;
    protected final int port;
    protected final String database;
    protected final String user;
    protected final String password;
    
    protected AbstractConnectionPool(String host, int port, String database, String user, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
    }
}
