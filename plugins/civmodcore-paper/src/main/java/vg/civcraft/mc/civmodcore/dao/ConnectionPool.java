package vg.civcraft.mc.civmodcore.dao;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

/**
 * Handy Connection Pool / Database wrapper for use by all plugins.
 *
 * @author ProgrammerDan
 */
public class ConnectionPool {

	private static final Logger LOGGER = Bukkit.getLogger();

	private final DatabaseCredentials credentials;

	private HikariDataSource datasource;

	/**
	 * Creates a new ConnectionPool based on a given set of credentials. Note that the credentials are not scrutinised,
	 * so you should make sure they're valid, or at least valid enough, otherwise expect exceptions or logger spam.
	 *
	 * @param credentials The credentials to connect with.
	 */
	public ConnectionPool(DatabaseCredentials credentials) {
		Preconditions.checkArgument(credentials != null,
				"Cannot create a ConnectionPool with a null set of credentials.");
		this.credentials = credentials;
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:" + credentials.getDriver() + "://" + credentials.getHostname() + ":" +
				credentials.getPort() + "/" + credentials.getDatabase());
		config.setConnectionTimeout(credentials.getConnectionTimeout());
		config.setIdleTimeout(credentials.getIdleTimeout());
		config.setMaxLifetime(credentials.getMaxLifetime());
		config.setMaximumPoolSize(credentials.getPoolSize());
		config.setUsername(credentials.getUsername());
		if (!Strings.isNullOrEmpty(credentials.getPassword())) {
			config.setPassword(credentials.getPassword());
		}
		this.datasource = new HikariDataSource(config);
		try (Connection connection = getConnection(); Statement statement = connection.createStatement();) {
			statement.executeQuery("SELECT 1;");
			LOGGER.info("Successfully connected to the database.");
		}
		catch (SQLException exception) {
			LOGGER.severe("Unable to connect to the database.");
			exception.printStackTrace();
			this.datasource = null;
		}
	}

	/**
	 * Creates a new ConnectionPool based on explicitly defined parameters. Note that these parameters are not
	 * scrutinised, so you should make sure they're valid, or at least valid enough, otherwise expect exceptions or
	 * logger spam.
	 *
	 * @param user The SQL user to connect as.
	 * @param pass The SQL user's password.
	 * @param host The hostname of the database.
	 * @param port The port to connect via.
	 * @param driver The jdbc driver to use to connect to the database.
	 * @param database The specific database to create and modify tables in.
	 * @param poolSize The maximum size of the connection pool (under 10 recommended)
	 * @param connectionTimeout The longest a query can run until it times out (1-5 seconds recommended)
	 * @param idleTimeout The longest a connection can sit idle before recycling (10 minutes recommended, check dbms)
	 * @param maxLifetime The longest a connection can exist in total. (2 hours recommended, check dbms)
	 */
	public ConnectionPool(String user, String pass, String host, int port, String driver, String database,
						  int poolSize, long connectionTimeout, long idleTimeout, long maxLifetime) {
		this(new DatabaseCredentials(user, pass, host, port, driver, database,
				poolSize, connectionTimeout, idleTimeout, maxLifetime));
	}

	/**
	 * Legacy support constructor to create a connection pool.
	 *
	 * @param logger The logger that would be logged to. This can now be null since ConnectionPool now
	 *     uses its own logger.
	 * @param user The SQL user to connect as.
	 * @param pass The SQL user's password.
	 * @param host The hostname of the database.
	 * @param port The port to connect via.
	 * @param database The specific database to create and modify tables in.
	 * @param poolSize The maximum size of the connection pool (under 10 recommended)
	 * @param connectionTimeout The longest a query can run until it times out (1-5 seconds recommended)
	 * @param idleTimeout The longest a connection can sit idle before recycling (10 minutes recommended, check dbms)
	 * @param maxLifetime The longest a connection can exist in total. (2 hours recommended, check dbms)
	 *
	 * @deprecated This is deprecated as it insists on a logger and does not allow you to specify a jdbc driver.
	 */
	@Deprecated
	public ConnectionPool(Logger logger, String user, String pass, String host, int port, String database,
						  int poolSize, long connectionTimeout, long idleTimeout, long maxLifetime) {
		this(user, pass, host, port, "mysql", database, poolSize, connectionTimeout, idleTimeout, maxLifetime);
	}

	/**
	 * Gets the credentials used for this ConnectionPool.
	 *
	 * @return Returns the credentials being used.
	 */
	public DatabaseCredentials getCredentials() {
		return this.credentials;
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
