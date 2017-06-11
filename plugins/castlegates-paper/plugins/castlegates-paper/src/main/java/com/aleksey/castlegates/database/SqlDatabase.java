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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.aleksey.castlegates.utils.ResourceHelper;

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
            ex.printStackTrace();
            return false;
        }
        try {
            this.connection = DriverManager.getConnection(jdbc);

            this.logger.log(Level.INFO, "Connected to the database!");
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

    public boolean checkConnection() {
    	if(isConnected()) return true;

		this.logger.log(Level.INFO, "Database went away, reconnecting.");

		return connect();
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

		this.logger.log(Level.INFO, "Applying patches to database...");

		try {
			applyPatches();
    	} catch (SQLException e) {
    		this.logger.log(Level.SEVERE, "Failed to apply patches.");
			e.printStackTrace();
			return false;
		}

		this.logger.log(Level.INFO, "Patches applied to database.");

		return true;
    }

    private void applyPatches() throws SQLException {
    	int patchIndex = 1;
    	String patchName = String.format("patch_%03d.txt", patchIndex);
    	ArrayList<String> scripts;
    	PatchSource source = new PatchSource(this);

    	while((scripts = ResourceHelper.readScriptList("/" + patchName)) != null) {
    		this.logger.log(Level.INFO, "Found patch " + patchName);

    		if(source.isExist(patchName)) {
    			this.logger.log(Level.INFO, "Skipping patch.");
    		} else {
	    		this.logger.log(Level.INFO, "Applying patch...");

	    		for(String script : scripts) {
	    			prepareStatement(script).execute();
	    		}

	    		PatchInfo patchInfo = new PatchInfo();
	    		patchInfo.patchName = patchName;
	    		patchInfo.appliedDate = new Timestamp(System.currentTimeMillis());

	    		source.insert(patchInfo);

	    		this.logger.log(Level.INFO, "Patch applied.");
    		}

    		patchName = String.format("patch_%03d.txt", ++patchIndex);
    	}
    }
}
