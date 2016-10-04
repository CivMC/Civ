package com.devotedmc.ExilePearl.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import com.devotedmc.ExilePearl.PearlLogger;

public class MySqlConnection {
	
	private final String host;
    private final int port;
    private final String db;
    private final String user;
    private final String password;
    private final PearlLogger logger;
    
    private Connection connection;

    /**
     * Creates a new MySqlConnection instance
     * @param host The database host
     * @param port The database port
     * @param db The database name
     * @param user The database user
     * @param password The database password
     * @param logger The logging instance
     */
    public MySqlConnection(final String host, final int port, final String db, final String user, final String password, final PearlLogger logger) {
        this.host = host;
        this.port = port;
        this.db = db;
        this.user = user;
        this.password = password;
        this.logger = logger;
    }

    /**
     * Connects to the database.
     */
    public boolean connect() {
        String jdbc = "jdbc:mysql://" + host + ":" + port + "/" + db + "?user=" + user + "&password=" + password;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
        	logger.log(Level.SEVERE, "Failed to initialize JDBC driver.");
        	ex.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection(jdbc);
            logger.log(Level.INFO, "Connected to the MySQL database.");
            return true;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to connect to the MySQL database.");
            return false;
        }
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        try {
            connection.close();
            connection = null;
            logger.log(Level.INFO, "Disconnected from the MySQL database.");
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to close MySQL database connection");
        }
    }

    
    /**
     * Checks if the database connection is valid
     * @return Connected
     */
    public boolean isConnected() {
    	if (connection == null) {
    		return false;
    	}
    	
        try {
            return connection.isValid(5);
        } catch (SQLException ex) {
        }
        return false;
    }
    

    /**
     * Prepare the SQL statements
     * @param sql The sqlStatement string
     * @return PreparedStatement
     */
    public PreparedStatement prepareStatement(String sqlStatement) {
        try {
            return connection.prepareStatement(sqlStatement);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to prepare SQL statement '%s'. ", sqlStatement);
        }
        return null;
    }
    
    
    /**
     * Executes a SQL query
     * @param query The query to execute
     */
    public void execute(String query) {
    	if (!isConnected()) {
    		logger.log(Level.SEVERE, "Failed to exectue SQL query. Database isn't connected.");
    		return;
    	}
    	
        try {
            connection.prepareStatement(query).executeUpdate();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to exectue SQL query.");
        }
    }
    
    public Connection getConnection() {
    	return connection;
    }
}
