package com.programmerdan.minecraft.civspy.database;

import java.util.UUID;
import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.logging.Level;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariConfig;

/**
 * Wrapper for Connection Pool, and holder for static strings.
 * 
 * @author ProgrammerDan
 */
public class Database {
	public static final String INIT_DATABASE =
			"CREATE TABLE IF NOT EXISTS spy_stats (" +
			"  id BIGINT NOT NULL AUTO_INCREMENT," +
			"  stat_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
			"  stat_key VARCHAR(64) NOT NULL," +
			"  server VARCHAR(64) DEFAULT NULL," +
			"  world VARCHAR(100) DEFAULT NULL," +
			"  chunk_x INT DEFAULT NULL," +
			"  chunk_z INT DEFAULT NULL," + 
			"  uuid VARCHAR(36) DEFAULT NULL," +
			"  numeric_value DOUBLE DEFAULT NULL," +
			"  string_value TEXT DEFAULT NULL," +
			"  CONSTRAINT pk_spy_stats PRIMARY KEY (id)," +
			"  INDEX idx_spy_stats_time_key USING BTREE (stat_time, stat_key)," +
			"  INDEX idx_spy_ext_key USING BTREE (server, world, chunk_x, chunk_z, uuid)" +
			");";
	
	public static final String INSERT_KEY =
			"INSERT INTO spy_stats (stat_time, stat_key, server, world, chunk_x, chunk_z, uuid) VALUES (?, ?, ?, ?, ?, ?, ?);";

	public static final String INSERT_STRING =
			"INSERT INTO spy_stats (stat_time, stat_key, string_value, server, world, chunk_x, chunk_z, uuid) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
	
	public static final String INSERT_NUMBER =
			"INSERT INTO spy_stats (stat_time, stat_key, numeric_value, server, world, chunk_x, chunk_z, uuid) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
	
	public static final String INSERT_COMBINED =
			"INSERT INTO spy_stats (stat_time, stat_key, string_value, numeric_value, server, world, chunk_x, chunk_z, uuid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

	private HikariDataSource datasource;
	
	private Logger log;
	
	/**
	 * Creates the Database connection pool backed by HikariCP.
	 * 
	 * @param log The logger to use
	 * @param user The user to connection as
	 * @param pass The password to use
	 * @param host The host to connect to
	 * @param port The port on the host to connect to
	 * @param database The database to use
	 * @param poolSize The maximum size of the connection pool
	 * @param connectionTimeout The longest a query can run until timeout occurs
	 * @param idleTimeout The longest a connection can sit idle before recycling
	 * @param maxLifetime The absolute longest length of time a connection can exist
	 */
	public Database(Logger log, String user, String pass, String host, int port, String database,
			int poolSize, long connectionTimeout, long idleTimeout, long maxLifetime) {
		this.log = log;
		if (user != null && host != null && port > 0 && database != null) {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
			config.setConnectionTimeout(connectionTimeout); // 1000l);
			config.setIdleTimeout(idleTimeout); //600000l);
			config.setMaxLifetime(maxLifetime); //7200000l);
			config.setMaximumPoolSize(poolSize); //10);
			config.setUsername(user);
			if (pass != null) {
				config.setPassword(pass);
			}
			this.datasource = new HikariDataSource(config);
			
			try {
				Connection connection = getConnection();
				PreparedStatement statement = connection.prepareStatement(Database.INIT_DATABASE);
				statement.execute();
				statement.close();
				connection.close();
			} catch (SQLException se) {
				log.log(Level.SEVERE, "Unable to initialize Database", se);
				this.datasource = null;
			}
		} else {
			this.datasource = null;
			log.log(Level.SEVERE, "Database not configured and is unavaiable");
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
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		available();
		this.datasource.close();
	}
	
	/**
	 * Quick test; either ends or throws an exception if data source isn't configured.
	 * @throws SQLException
	 */
	public void available() throws SQLException {
		if (this.datasource == null) {
			throw new SQLException("No Datasource Available");
		}
	}
	
	/**
	 * Inserts a simple sample of _just_ a stat key, nothing more.
	 * 
	 * @param key The stat key to insert with no additional data.
	 * @return # of records inserted
	 */
	public int insertData(String key) {
		return insertData(key, null, null, null, null, null, null, null, null, null);
	}
	
	/**
	 * Inserts a simple sample of a stat key with a string label.
	 * 
	 * @param key The stat key to insert
	 * @param value The string_value to insert.
	 * @return # of records inserted
	 */
	public int insertData(String key, String value) {
		return insertData(key, null, null, null, null, null, value, null, null, null);
	}
	
	/**
	 * Inserts a simple sample of a stat key with a numeric value.
	 * 
	 * @param key The stat key to insert
	 * @param value The numeric_value to insert.
	 * @return # of records inserted
	 */
	public int insertData(String key, Number value) {
		return insertData(key, null, null, null, null, null, null, value, null, null);
	}
	
	/**
	 * Inserts a simple sample of a stat key with a string label and a numeric value
	 * 
	 * @param key The stat key to insert
	 * @param sValue The string_value to insert.
	 * @param nValue The numeric_value to insert.
	 * @return # of records inserted
	 */
	public int insertData(String key, String sValue, Number nValue) {
		return insertData(key, null, null, null, null, null, sValue, nValue, null, null);
	}
	
	/**
	 * Same as {@link #insertData(String, String, Number)} but with ability to set 
	 * the datapoint time and which connection to use (if null, manages requesting on).
	 * @param key
	 * @param sValue
	 * @param nValue
	 * @param time The time of sampling 
	 * @param connection The connection or null if a new connection is requested.
	 * @return # of records inserted
	 */
	public int insertData(String key, String sValue, Number nValue, Long time, Connection connection) {
		return insertData(key, null, null, null, null, null, sValue, nValue, time, connection);
	}
	public int insertData(String key, UUID uuid) {
		return insertData(key, null, null, null, null, uuid, null, null, null, null);
	}
	public int insertData(String key, UUID uuid, String value) {
		return insertData(key, null, null, null, null, uuid, value, null, null, null);
	}
	public int insertData(String key, UUID uuid, Number value) {
		return insertData(key, null, null, null, null, uuid, null, value, null, null);
	}
	public int insertData(String key, UUID uuid, String sValue, Number nValue) {
		return insertData(key, null, null, null, null, uuid, sValue, nValue, null, null);
	}
	public int insertData(String key, UUID uuid, String sValue, Number nValue, Long time, Connection connection) {
		return insertData(key, null, null, null, null, uuid, sValue, nValue, time, connection);
	}
	public int insertData(String key, String server, String world, Integer chunk_x, Integer chunk_z) {
		return insertData(key, world, server, chunk_x, chunk_z, null, null, null, null, null);
	}
	public int insertData(String key, String server, String world, Integer chunk_x, Integer chunk_z, String value) {
		return insertData(key, world, server, chunk_x, chunk_z, null, value, null, null, null);
	}
	public int insertData(String key, String server, String world, Integer chunk_x, Integer chunk_z, Number value) {
		return insertData(key, world, server, chunk_x, chunk_z, null, null, value, null, null);
	}
	public int insertData(String key, String server, String world, Integer chunk_x, Integer chunk_z, String sValue, Number nValue) {
		return insertData(key, world, server, chunk_x, chunk_z, null, sValue, nValue, null, null);
	}
	public int insertData(String key, String server, String world, Integer chunk_x, Integer chunk_z, String sValue, Number nValue,
			Long time, Connection connection) {
		return insertData(key, world, server, chunk_x, chunk_z, null, sValue, nValue, time, connection);
	}

	public int insertData(String key, String server, String world, Integer chunk_x, Integer chunk_z, UUID uuid) {
		return insertData(key, world, server, chunk_x, chunk_z, uuid, null, null, null, null);
	}
	public int insertData(String key, String server, String world, Integer chunk_x, Integer chunk_z, UUID uuid, String value) {
		return insertData(key, world, server, chunk_x, chunk_z, uuid, value, null, null, null);
	}
	public int insertData(String key, String server, String world, Integer chunk_x, Integer chunk_z, UUID uuid, Number value) {
		return insertData(key, world, server, chunk_x, chunk_z, uuid, null, value, null, null);
	}
	public int insertData(String key, String server, String world, Integer chunk_x, Integer chunk_z, UUID uuid, String sValue, Number nValue) {
		return insertData(key, world, server, chunk_x, chunk_z, uuid, sValue, nValue, null, null);
	}
	
	/**
	 * Fully specified insertData.
	 * 
	 * @param key The stat_key to use
	 * @param server The server to use
	 * @param world The world to use
	 * @param chunk_x The chunk_x of investigation
	 * @param chunk_z The chunk_z of investigation
	 * @param uuid The player uuid involved, if any
	 * @param sValue The string value on the stats indicator
	 * @param nValue The number value on the stats indicator
	 * @param time When did sample occur or aggregate bucket was started
	 * @param connection A connection object to ignore or use -- ignore if empty, use if not.
	 * @return # of records inserted.
	 */
	public int insertData(String key, String server, String world, Integer chunk_x, Integer chunk_z, UUID uuid, 
			String sValue, Number nValue, Long time, Connection connection) {
		if (key == null) return -1;
		boolean iOwn = connection == null;
		try {
			connection = iOwn ? getConnection() : connection;
			time = time == null ? System.currentTimeMillis() : time;
			PreparedStatement statement = null;
			int o = 0;
			if (sValue == null) {
				if (nValue == null) {
					statement = connection.prepareStatement(Database.INSERT_KEY);
				} else {
					statement = connection.prepareStatement(Database.INSERT_NUMBER);
					statement.setDouble(3, nValue.doubleValue());
					o = 1;
				}
			} else {
				if (nValue == null) {
					statement = connection.prepareStatement(Database.INSERT_STRING);
					statement.setString(3, sValue);
					o = 1;
				} else {
					statement = connection.prepareStatement(Database.INSERT_COMBINED);
					statement.setString(3, sValue);
					statement.setDouble(4, nValue.doubleValue());
					o = 2;
				}
			}
			statement.setTimestamp(1, new Timestamp(time));
			statement.setString(2, key.substring(0, Math.min(64, key.length())));

			if (server != null) {
				statement.setString(3 + o, server.substring(0, Math.min(64, server.length())));
			} else {
				statement.setNull(3 + o, Types.VARCHAR);
			}
			if (world != null) {
				statement.setString(4 + o, world.substring(0, Math.min(100, world.length())));
			} else {
				statement.setNull(4 + o, Types.VARCHAR);
			}
			if (chunk_x != null) {
				statement.setInt(5 + o, chunk_x);
			} else {
				statement.setNull(5 + o, Types.INTEGER);
			}
			if (chunk_z != null) {
				statement.setInt(6 + o, chunk_z);
			} else {
				statement.setNull(6 + o, Types.INTEGER);
			}
			if (uuid != null) {
				statement.setString(7 + o, uuid.toString());
			} else {
				statement.setNull(7 + o, Types.VARCHAR);
			}

			int results = statement.executeUpdate();
			SQLWarning warning = statement.getWarnings();
			while (warning != null) {
				log.log(Level.WARNING, "Problem inserting: {0} {1}", new Object[]{warning.getErrorCode(), warning});
				warning = warning.getNextWarning();
			}
			return results;
		} catch (SQLException se) {
			log.log(Level.SEVERE, "Unable to insert data", se);
			return -1;
		} finally {
			try {
				if (iOwn) connection.close();
			} catch(SQLException se) {
				log.log(Level.SEVERE, "Failed to close connection?", se);
			}
		}
	}

	/**
	 * Does all that {@link #insertData(String, String, String, Integer, Integer, UUID, String, Number, Long, Connection)} does and more
	 * 
	 * If a PreparedStatement is passed in, attempts to add this new data batch to the mix.
	 * 
	 * If no PreparedStatement is found, one is made on either the provided connection or a new one, then starts a new batch.
	 * 
	 * If you are using this class to manage your batch, use the returned PreparedStatement in the
	 * next call to this method for maximum success.
	 * 
	 * When the batch is done just call {@link #batchExecute(Statement, boolean)} on the PrepareStatement.
	 * 
	 * @param key
	 * @param server
	 * @param world
	 * @param chunk_x
	 * @param chunk_z
	 * @param uuid
	 * @param sValue
	 * @param nValue
	 * @param time The time of sampling / aggregation or null to use the time-at-batching
	 * @param connection
	 * @param statement
	 * @return A PreparedStatement suitable for continued use
	 */
	@SuppressWarnings("resource")
	public PreparedStatement batchData(String key, String server, String world, Integer chunk_x, Integer chunk_z, UUID uuid, 
			String sValue, Number nValue, Long time, Connection connection, PreparedStatement statement) {
		if (key == null) {
			log.log(Level.SEVERE, "Key given was null, cannot batch this data.");
			return statement;
		}
		boolean iOwn = (connection == null);
		boolean newState = (statement == null);
		try {
			connection = iOwn && newState ? getConnection() : newState ? connection: statement.getConnection();
			time = time == null ? System.currentTimeMillis() : time;
			statement = newState ? connection.prepareStatement(Database.INSERT_COMBINED) : statement;
		
			statement.setTimestamp(1, new Timestamp(time));
			statement.setString(2, key.substring(0, Math.min(64, key.length())));
			if (sValue != null) {
				statement.setString(3, sValue);
			} else {
				statement.setNull(3, Types.VARCHAR);
			}
			if (nValue != null) {
				statement.setDouble(4, nValue.doubleValue());
			} else {
				statement.setNull(4, Types.NUMERIC);
			}
			if (server != null) {
				statement.setString(5, server.substring(0, Math.min(64, server.length())));
			} else {
				statement.setNull(5, Types.VARCHAR);
			}
			if (world != null) {
				statement.setString(6, world.substring(0, Math.min(100, world.length())));
			} else {
				statement.setNull(6, Types.VARCHAR);
			}
			if (chunk_x != null) {
				statement.setInt(7, chunk_x);
			} else {
				statement.setNull(7, Types.INTEGER);
			}
			if (chunk_z != null) {
				statement.setInt(8, chunk_z);
			} else {
				statement.setNull(8, Types.INTEGER);
			}
			if (uuid != null) {
				statement.setString(9, uuid.toString());
			} else {
				statement.setNull(9, Types.VARCHAR);
			}

			statement.addBatch();
		} catch (SQLException se) {
			log.log(Level.SEVERE, "Unable to insert data", se);
		}
		return statement;
	}

	/**
	 * Handles committing a batch created using the batch commands.
	 * 
	 * @param statement The statement with an open batch
	 * @param closeConnection Close the connection (return to pool) when done or no?
	 * 
	 * @return Array of results from all batch statements.
	 */
	public int[] batchExecute(Statement statement, boolean closeConnection) {
		if (statement == null) return null;
		try {
			if (statement.isClosed()) return null;

			int[] results = statement.executeBatch();
			SQLWarning warning = statement.getWarnings();
			while (warning != null) {
				log.log(Level.WARNING, "Problem executing: {0} {1}", new Object[]{warning.getErrorCode(), warning});
				warning = warning.getNextWarning();
			}
			return results;
		} catch (SQLException se) {
			log.log(Level.SEVERE, "Unable to execute this batch!", se);
			return null;
		} finally {
			if (closeConnection) {
				try {
					statement.getConnection().close();
				} catch (SQLException se2) {
					log.log(Level.WARNING, "Failed to close the connection post batch", se2);
				}
			} else {
				try {
					statement.close();
				} catch (SQLException se3) {
					log.log(Level.WARNING, "Failed to close the statement post batch", se3);
				}
			}
		}
	}
}
