package sh.okx.railswitch.database;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnectionPool implements ConnectionPool {
    private Path file;
    
    public SQLiteConnectionPool(File dataFolder, String file) {
        this.file = dataFolder.toPath().resolve(file);
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + file);
    }
}
