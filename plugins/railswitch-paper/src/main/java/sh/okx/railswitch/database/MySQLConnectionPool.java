package sh.okx.railswitch.database;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLConnectionPool implements ConnectionPool {
    private final HikariDataSource hikariDataSource;
    
    public MySQLConnectionPool(String host, int port, String database, String user, String password) {
        hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        hikariDataSource.setUsername(user);
        hikariDataSource.setPassword(password);
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }
}
