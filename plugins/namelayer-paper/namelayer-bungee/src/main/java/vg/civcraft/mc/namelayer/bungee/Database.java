package vg.civcraft.mc.namelayer.bungee;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Thin wrapper around HikariCP datasource / pool.
 * 
 * I recommend a configuration that fits your database needs. If you aren't sure what that looks like, consider the following:
 * 
 * Pool Size of no more then 10; this is the maximum number of concurrent connections that will be alive on the pool. Keep in mind this is limited by the
 *    number of connections the database is configured to support, and exceeding _that_ limit will begin to fail new connection attempts, causing no end of
 *    trouble. Make sure your database instance is properly configured to fit your needs!
 *    
 * Connection Timeout; make this as short as your longest running query. Give some headroom in case of database congestion. 10s is a good value, but 30s may
 *    be more appropriate for database queries that are very slow (big async processes, etc.)
 * 
 * Idle Timeout; Again, this should be paired against your database configuration's idle timeout. If your database drops connections that are idle before your
 *    connection pool does, this can cause your pool to deliver dead connections. That's a bad day. Consider 600 seconds; and configure your database appropriately.
 *    
 * Max Lifetime; Sometimes connections just grow old. It can be useful to just, well, recycle them; throwing away old ones and getting new ones.
 *    Typically you will configure this to be longer then the Idle Timeout. Do not make it overly short. Setting up a connection is expensive and impacts
 *    all other pools attempting to connect to the same database. consider 7200 seconds; well beyond idle timeout, so only well-used connections will last this long.
 * 
 * @author ProgrammerDan
 * @since 8/29/2016
 */
public class Database {
	private HikariDataSource datasource;
	
	private Logger logger;
	
	/**
	 * Configure the database pool.
	 * 
	 * @param logger A JUL logger to use for error reporting, etc.
	 * @param user The user to connect as
	 * @param pass The password to use
	 * @param host The host to connect to
	 * @param port The port to connect to
	 * @param database The database to use
	 * @param poolSize How many connections at most to keep in the connection pool awaiting activity. Consider this against your Database's max connections across all askers.
	 * @param connectionTimeout How long will a single connection wait for activity before timing out. 
	 * @param idleTimeout How long will a connection wait in the pool, typically, inactive.
	 * @param maxLifetime How long will a connection be kept alive at maximum
	 */
	public Database(Logger logger, String user, String pass, String host, int port, String database,
			int poolSize, long connectionTimeout, long idleTimeout, long maxLifetime) {
		this.logger = logger;
		if (user != null && host != null && port > 0 && database != null) {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
			config.setConnectionTimeout(connectionTimeout); //10000l);
			config.setIdleTimeout(idleTimeout); //600000l);
			config.setMaxLifetime(maxLifetime); //7200000l);
			config.setMaximumPoolSize(poolSize); //10);
			config.setUsername(user);
			if (pass != null) {
				config.setPassword(pass);
			}
			this.datasource = new HikariDataSource(config);
			
			try { //No-op test.
				Connection connection = getConnection();
				Statement statement = connection.createStatement();
				statement.execute("SELECT 1");
				statement.close();
				connection.close();
			} catch (SQLException se) {
				this.logger.log(Level.SEVERE, "Unable to initialize Database", se);
				this.datasource = null;
			}
		} else {
			this.datasource = null;
			this.logger.log(Level.SEVERE, "Database not configured and is unavaiable");
		}
	}
	
	/**
	 * Whenever you want to do anything SQL-y, just call this method. It'll give you a connection, and from there you can do
	 * whatever you need. Remember to .close() the connection when you are done. 
	 * 
	 * With a connection pool, this simply returns it back to the pool, or recycles it if it's too old.
	 * 
	 * Don't keep a connection for longer then you have to; but since you own a connection while you have the reference, you
	 * can do cool stuff like longer-term batching and other prepared sorts of things, which is helpful.
	 * 
	 * @return A standard SQL Connection from the Pool
	 * @throws SQLException If this datasource isn't available or there is an error getting a connection from the pool. If this happens you're in for it.
	 */
	public Connection getConnection() throws SQLException {
		available();
		return this.datasource.getConnection();
	}
	
	/**
	 * Closes the entire connection pool down. Don't call this until you're absolutely certain you're done with the database.
	 * 
	 * @throws SQLException If datasource is already closed.
	 */
	public void close() throws SQLException {
		available();
		this.datasource.close();
		this.datasource = null; // available will now fail.
	}
	
	/**
	 * Quick test; either ends or throws an exception if data source isn't configured.
	 * Used internally on {@link #getConnection()} so most likely you don't need to use this.
	 * 
	 * @throws SQLException If the datasource is gone.
	 */
	public void available() throws SQLException {
		if (this.datasource == null) {
			throw new SQLException("No Datasource Available");
		}
	}
}
