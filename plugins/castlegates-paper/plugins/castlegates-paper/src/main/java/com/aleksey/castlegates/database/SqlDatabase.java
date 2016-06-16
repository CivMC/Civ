/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.aleksey.castlegates.utils.ResourceHelper;
import com.avaje.ebeaninternal.server.lib.sql.DataSourceException;

public class SqlDatabase {
	private String host;
    private int port;
    private String db;
    private String user;
    private String password;
    private Logger logger;
    private Connection connection;
    
    public SqlDatabase(String host, int port, String db, String user, String password, Logger logger) {
        this.host = host;
        this.port = port;
        this.db = db;
        this.user = user;
        this.password = password;
        this.logger = logger;
    }
    
    public boolean connect() {
        String jdbc = "jdbc:mysql://" + host + ":" + port + "/" + db + "?user=" + user + "&password=" + password;
        
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            throw new DataSourceException("Failed to initialize JDBC driver.");
        }
        try {
            this.connection = DriverManager.getConnection(jdbc);
            
            this.logger.log(Level.INFO, "Connected to database!");
            return true;
        } catch (SQLException ex) { //Error handling below:
            this.logger.log(Level.SEVERE, "Could not connnect to the database! Connection string: " + jdbc, ex);
            return false;
        }
    }
    
    public void close() {
        try {
            this.connection.close();
        } catch (SQLException ex) {
            this.logger.log(Level.SEVERE, "An error occured while closing the connection.", ex);
        }
    }
    
    public boolean isConnected() {
        try {
            return this.connection.isValid(5);
        } catch (SQLException ex) {
            this.logger.log(Level.SEVERE, "isConnected error!", ex);
        }
        return false;
    }
    
    public PreparedStatement prepareStatement(String sqlStatement) throws SQLException {
        return this.connection.prepareStatement(sqlStatement);
    }

    public PreparedStatement prepareStatementWithReturn(String sqlStatement) throws SQLException {
        return this.connection.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS);
    }
    
    public boolean initDb() {
    	this.logger.log(Level.INFO, "Database initialization started...");
    	
    	ArrayList<String> list = ResourceHelper.readScriptList("/create_db.txt");
    	
		for(String script : list) {
			try {
				prepareStatement(script).execute();
	    	} catch (SQLException e) {
	    		this.logger.log(Level.SEVERE, "Database is NOT initialized.");
	    		this.logger.log(Level.SEVERE, "Failed script: \n" + script);
				e.printStackTrace();
				return false;
			}
		}
		
		this.logger.log(Level.INFO, "Database initialized.");
		
		return true;
    }
}
