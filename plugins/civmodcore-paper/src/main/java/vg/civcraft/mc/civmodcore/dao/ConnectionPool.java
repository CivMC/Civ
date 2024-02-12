package vg.civcraft.mc.civmodcore.dao;

import com.google.common.base.Strings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
	public ConnectionPool(@Nonnull final DatabaseCredentials credentials) {
		this.credentials = Objects.requireNonNull(credentials,
				"Cannot create a ConnectionPool with a null set of credentials.");
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:" + credentials.driver() + "://" + credentials.host() + ":" +
				credentials.port() + "/" + credentials.database());
		config.setConnectionTimeout(credentials.connectionTimeout());
		config.setIdleTimeout(credentials.idleTimeout());
		config.setMaxLifetime(credentials.maxLifetime());
		config.setMaximumPoolSize(credentials.poolSize());
		config.setUsername(credentials.username());
		if (!Strings.isNullOrEmpty(credentials.password())) {
			config.setPassword(credentials.password());
		}
		this.datasource = new HikariDataSource(config);
		try (final Connection connection = getConnection();
			 final Statement statement = connection.createStatement()) {
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
	public ConnectionPool(final String user,
						  final String pass,
						  final String host,
						  final int port,
						  final String driver,
						  final String database,
						  final int poolSize,
						  final long connectionTimeout,
						  final long idleTimeout,
						  final long maxLifetime) {
		this(new DatabaseCredentials(user, pass, host, port, driver, database,
				poolSize, connectionTimeout, idleTimeout, maxLifetime));
	}

	/**
	 * Gets the credentials used for this ConnectionPool.
	 *
	 * @return Returns the credentials being used.
	 */
	@Nonnull
	public DatabaseCredentials getCredentials() {
		return this.credentials;
	}

	/**
	 * Gets a single connection from the pool for use. Checks for null database first.
	 *
	 * @return A new Connection
	 * @throws SQLException
	 */
	@Nonnull
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
	@Nullable
	HikariDataSource getHikariDataSource() {
		return this.datasource;
	}

}
