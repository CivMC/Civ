package vg.civcraft.mc.civchat2.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {

	private String host;

	private int port;

	private String db;

	private String user;

	private String password;

	private Logger logger;

	private Connection connection;

	public Database(String host, int port, String db, String user, String password, Logger logger) {

		this.host = host;
		this.port = port;
		this.db = db;
		this.user = user;
		this.password = password;
		this.logger = logger;
	}

	public String getDb() {

		return db;
	}

	public String getHost() {

		return host;
	}

	public String getPassword() {

		return password;
	}

	public String getUser() {

		return user;
	}

	/**
	 * Connects to the database.
	 *
	 * @since 0.1
	 */
	public boolean connect() {

		String jdbc = "jdbc:mysql://" + host + ":" + port + "/" + db + "?user=" + user + "&password=" + password;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to initialize JDBC driver.");
		}
		try {
			connection = DriverManager.getConnection(jdbc);
			this.logger.log(Level.INFO, "Connected to database!");
			return true;
		} catch (SQLException ex) {
			this.logger.log(Level.SEVERE, "Could not connnect to the database!", ex);
			return false;
		}
	}

	/**
	 * Closes the database connection.
	 *
	 * @since 0.1
	 */
	public void close() {

		try {
			connection.close();
		} catch (SQLException ex) {
			this.logger.log(Level.SEVERE, "An error occured while closing the connection.", ex);
		}
	}

	/**
	 * Are we connected to the database?
	 *
	 * @return Connected
	 * @throws SQLException
	 */
	public boolean isConnected() {

		try {
			return connection.isValid(5);
		} catch (SQLException ex) {
			this.logger.log(Level.SEVERE, "isConnected error!", ex);
		}
		return false;
	}

	/**
	 * Prepare the SQL statements
	 *
	 * @return PreparedStatement
	 * @throws SQLException
	 */
	public PreparedStatement prepareStatement(String sqlStatement) {

		try {
			if (!isConnected()) {
				connect();
			}
			return connection.prepareStatement(sqlStatement);
		} catch (SQLException ex) {
			this.logger.log(Level.SEVERE, "Failed to prepare statement! " + sqlStatement, ex);
		}
		return null;
	}

	public void execute(String sql) {

		try {
			if (isConnected()) {
				connection.prepareStatement(sql).executeUpdate();
			} else {
				connect();
				execute(sql);
			}
		} catch (SQLException ex) {
			this.logger.log(Level.SEVERE, "Could not execute SQL statement!", ex);
		}
	}
}
