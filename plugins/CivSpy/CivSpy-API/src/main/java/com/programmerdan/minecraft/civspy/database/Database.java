package com.programmerdan.minecraft.civspy.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import com.zaxxer.hikari.HikariDataSource;

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
			"  numeric_value NUMERIC DEFAULT NULL," +
			"  string_value TEXT DEFAULT NULL," +
			"  CONSTRAINT pk_spy_stats PRIMARY KEY (id)," +
			"  INDEX idx_spy_stats_time_key USING BTREE (stat_time, stat_key)," +
			"  INDEX idx_spy_ext_key USING BTREE (server, world, chunk_x, chunk_z, uuid)" +
			");";
	
	public static final String INSERT_KEY =
			"INSERT INTO spy_stats (stat_time, stat_key, server, world, chunk_x, chunk_z, uuid) VALUES (?, ?, ?, ?, ?, ?, ?);";

	public static final String INSERT_STRING =
			"INSERT INTO spy_stats (stat_time, stat_key, server, world, chunk_x, chunk_z, uuid, string_value) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
	
	public static final String INSERT_NUMBER =
			"INSERT INTO spy_stats (stat_time, stat_key, server, world, chunk_x, chunk_z, uuid, numeric_value) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
	
	public static final String INSERT_COMBINED =
			"INSERT INTO spy_stats (stat_time, stat_key, server, world, chunk_x, chunk_z, uuid, string_value, numeric_value) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	private HikariDataSource datasource;
	
	private Logger log;
	
	public Database(Logger log, String user, String pass, String host, int port, String database) {
		this.log = log;
		if (user != null && host != null && port > 0 && database != null) {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + name);
			config.setConnectionTimeout(1000l);
			config.setIdleTimeout(600000l);
			config.setMaxLifetime(7200000l);
			config.setMaximumPoolSize(10);
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
	
	public Connection getConnection() throws SQLException {
		available();
		return this.datasource.getConnection();
	}
	
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
	
	public int insertData(String key) {
		return insertData(key, null, null, null, null, null, null, null, null, null);
	}
	public int insertData(String key, String value) {
		return insertData(key, null, null, null, null, null, value, null, null, null);
	}
	public int insertData(String key, Number value) {
		return insertData(key, null, null, null, null, null, null, value, null, null);
	}
	public int insertData(String key, String sValue, Number nValue) {
		return insertData(key, null, null, null, null, null, sValue, nValue, null, null);
	}
	public int insertData(String key, String sValue, Number nValue, Long time, Connection connection) {
		return insertData(key, null, null, null, null, null, sValue, nValue, time, connection);
	}
	public int insertData(String key, String world, String server, Integer chunk_x, Integer chunk_z) {
		return insertData(key, world, server, chunk_x, chunk_z, null, null, null, null, null);
	}
	public int insertData(String key, String world, String server, Integer chunk_x, Integer chunk_z, String value) {
		return insertData(key, world, server, chunk_x, chunk_z, null, value, null, null, null);
	}
	public int insertData(String key, String world, String server, Integer chunk_x, Integer chunk_z, Number value) {
		return insertData(key, world, server, chunk_x, chunk_z, null, null, value, null, null);
	}
	public int insertData(String key, String world, String server, Integer chunk_x, Integer chunk_z, String sValue, Number nValue) {
		return insertData(key, world, server, chunk_x, chunk_z, null, sValue, nValue, null, null);
	}

	public int insertData(String key, String server, String world, Integer chunk_x, Integer chunk_z, UUID uuid, 
			String sValue, Number nValue, Long time, Connection connection) {
		if (key == null) return -1;
		boolean iOwn = connection == null;
		try {
			connection = iOwn ? getConnection() : connection;
			time = time == null ? System.currentTimeMillis() : time;
			PreparedStatement statement = null;
			if (sValue == null) {
				if (nValue == null) {
					statement = connection.prepareStatement(Database.INSERT_KEY);
				} else {
					statement = connection.prepareStatement(Database.INSERT_NUMBER);
					statement.setDouble(3, nValue.doubleValue());
				}
			} else {
				if (nValue == null) {
					statement = connection.prepareStatement(Database.INSERT_STRING);
					statement.setString(3, sValue);
				} else {
					statement = connection.prepareStatement(Database.INSERT_COMBINED);
					statement.setString(3, sValue);
					statement.setDouble(4, nValue.doubleValue());
				}
			}
			statement.setLong(1, time);
			statement.setString(2, key);
			int results = statement.executeUpdate();
			SQLWarning warning = statement.getWarnings();
			while (warning != null) {
				log.log(Level.WARNING, warning.getErrorCode(), warning);
				warning = warning.getNextWarning();
			}
			return results;
		} catch (SQLException se) {
			log.log(Level.SEVERE, "Unable to insert data", se);
			return -1;
		} finally {
			if (iOwn) connection.close();
		}
	}
}
