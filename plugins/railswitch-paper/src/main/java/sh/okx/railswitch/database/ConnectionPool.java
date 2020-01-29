package sh.okx.railswitch.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface for creating connections to databases for {@link RailSwitchDatabase}
 */
public interface ConnectionPool {
    
    /**
     * Gets the connection for this setup.
     *
     * @return Returns the connection.
     * @throws SQLException Throws an exception if there's an error during the connection.
     */
    Connection getConnection() throws SQLException;
    
}
