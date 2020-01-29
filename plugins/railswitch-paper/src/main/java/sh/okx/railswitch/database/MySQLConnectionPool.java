package sh.okx.railswitch.database;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database connection implementation for MySQL databases.
 */
public class MySQLConnectionPool implements ConnectionPool {
    
    private final HikariDataSource hikariDataSource;
    
    /**
     * Sets up a connection to a MySQL database.
     *
     * @param host The connection address
     * @param port The connection port
     * @param database The inner-database to use
     * @param username The username to login with
     * @param password The password to authenticate the login
     */
    public MySQLConnectionPool(String host, int port, String database, String username, String password) {
        hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        hikariDataSource.setUsername(username);
        hikariDataSource.setPassword(password);
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }
    
}
