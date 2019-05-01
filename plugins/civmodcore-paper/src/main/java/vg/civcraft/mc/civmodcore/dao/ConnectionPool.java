package vg.civcraft.mc.civmodcore.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Handy Connection Pool / Database wrapper for use by all plugins.
 * 
 * @author ProgrammerDan
 *
 */
public class ConnectionPool {

	private HikariDataSource datasource;

	private Logger logger;

	/**
	 * Creates the Database connection pool backed by HikariCP.
	 * 
	 * @param log
	 *            The logger to use
	 * @param user
	 *            The user to connection as
	 * @param pass
	 *            The password to use
	 * @param host
	 *            The host to connect to
	 * @param port
	 *            The port on the host to connect to
	 * @param database
	 *            The database to use
	 * @param poolSize
	 *            The maximum size of the connection pool (< 10 recommended)
	 * @param connectionTimeout
	 *            The longest a query can run until timeout occurs (1-5s recommended)
	 * @param idleTimeout
	 *            The longest a connection can sit idle before recycling (10min recom. check dbms config)
	 * @param maxLifetime
	 *            The absolute longest length of time a connection can exist (2hr recom. check dbms config)
	 */
	public ConnectionPool(Logger log, String user, String pass, String host, int port, String database, int poolSize,
			long connectionTimeout, long idleTimeout, long maxLifetime) {
		this.logger = log;
		if (user != null && host != null && port > 0 && database != null) {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
			config.setConnectionTimeout(connectionTimeout); // 1000l);
			config.setIdleTimeout(idleTimeout); // 600000l);
			config.setMaxLifetime(maxLifetime); // 7200000l);
			config.setMaximumPoolSize(poolSize); // 10);
			config.setUsername(user);
			if (pass != null) {
				config.setPassword(pass);
			}
			this.datasource = new HikariDataSource(config);

			try (Connection connection = getConnection(); Statement statement = connection.createStatement();) {
				statement.executeQuery("SELECT 1");
			} catch (SQLException se) {
				logger.log(Level.SEVERE, "Unable to initialize Database", se);
				this.datasource = null;
			}
		} else {
			this.datasource = null;
			logger.log(Level.SEVERE, "Database not configured and is unavaiable");
		}
	}

	/**
	 * Gets a single connection from the pool for use. Checks for null database first.
	 * 
	 * @return A new Connection
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		available();
		return this.datasource.getConnection();
	}

	/**
	 * Closes all connections and this connection pool.
	 * 
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		available();
		this.datasource.close();
		this.datasource = null;
	}

	/**
	 * Quick test; either ends or throws an exception if data source isn't configured.
	 * 
	 * @throws SQLException
	 */
	public void available() throws SQLException {
		if (this.datasource == null) {
			throw new SQLException("No Datasource Available");
		}
	}

	/**
	 * Available for direct use within this package, use the provided public methods for anything else
	 * 
	 * @return DataSource being used
	 */
	HikariDataSource getHikariDataSource() {
		return datasource;
	}
}
