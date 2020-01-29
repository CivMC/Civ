package sh.okx.railswitch.database;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection implementation for SQLite databases.
 */
public class SQLiteConnectionPool implements ConnectionPool {
    
    private Path file;
    
    /**
     * Sets up a connection to an SQLite database.
     *
     * @param folder The folder that the database is located in
     * @param file The database file itself
     */
    public SQLiteConnectionPool(File folder, String file) {
        this.file = folder.toPath().resolve(file);
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + file);
    }
    
}
