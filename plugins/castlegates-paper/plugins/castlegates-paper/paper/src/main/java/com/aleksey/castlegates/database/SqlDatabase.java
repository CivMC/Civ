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

public class SqlDatabase {
	private final String _host;
    private final int _port;
    private final String _db;
    private final String _user;
    private final String _password;
    private final Logger _logger;
    private Connection _connection;

    public SqlDatabase(String host, int port, String db, String user, String password, Logger logger) {
        _host = host;
        _port = port;
        _db = db;
        _user = user;
        _password = password;
        _logger = logger;
    }

    public boolean connect() {
        String jdbc = "jdbc:mysql://" + _host + ":" + _port + "/" + _db + "?user=" + _user + "&password=" + _password;

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        try {
            _connection = DriverManager.getConnection(jdbc);

            _logger.log(Level.INFO, "Connected to the database!");
            return true;
        } catch (SQLException ex) { //Error handling below:
            _logger.log(Level.SEVERE, "Could not connnect to the database! Connection string: " + jdbc, ex);
            return false;
        }
    }

    public void close() {
        try {
            _connection.close();
        } catch (SQLException ex) {
            _logger.log(Level.SEVERE, "An error occured while closing the connection.", ex);
        }
    }

    public boolean isConnected() {
        try {
            return _connection.isValid(5);
        } catch (SQLException ex) {
            _logger.log(Level.SEVERE, "isConnected error!", ex);
        }
        return false;
    }

    public boolean checkConnection() {
    	if(isConnected()) return true;

		_logger.log(Level.INFO, "Database went away, reconnecting.");

		return connect();
    }

    public PreparedStatement prepareStatement(String sqlStatement) throws SQLException {
        return _connection.prepareStatement(sqlStatement);
    }

    public PreparedStatement prepareStatementWithReturn(String sqlStatement) throws SQLException {
        return _connection.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS);
    }

    public boolean initDb() {
    	_logger.log(Level.INFO, "Database initialization started...");

    	ArrayList<String> list = ResourceHelper.readScriptList("/create_db.txt");

        if (list == null) {
            _logger.log(Level.SEVERE, "Resource create_db.txt is not found.");
            return false;
        }

		for(String script : list) {
			try (PreparedStatement statement = prepareStatement(script)) {
				statement.execute();
	    	} catch (SQLException e) {
	    		_logger.log(Level.SEVERE, "Database is NOT initialized.");
	    		_logger.log(Level.SEVERE, "Failed script: \n" + script);
				e.printStackTrace();
				return false;
			}
		}

		_logger.log(Level.INFO, "Database initialized.");

		return true;
    }
}
