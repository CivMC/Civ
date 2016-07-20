package com.programmerdan.minecraft.civspy.database;

package com.programmerdan.minecraft.wordbank.data;

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
	public static final String init =
			"CREATE TABLE IF NOT EXISTS spy_stats (" +
			"  id BIGINT NOT NULL AUTO_INCREMENT," +
			"  stat_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
			"  stat_key VARCHAR(64) NOT NULL," +
			"  numeric_value NUMERIC," +
			"  string_value TEXT," +
			"  CONSTRAINT pk_spy_stats PRIMARY KEY (id)," +
			"  INDEX idx_spy_stats_time_key USING BTREE (stat_time, stat_key)" +
			");";
	
	public static final String insert_string =
			"INSERT INTO spy_stats (stat_time, stat_key, string_value) VALUES (?, ?, ?);";
	
	public static final String insert_number =
			"INSERT INTO spy_stats (stat_time, stat_key, numeric_value) VALUES (?, ?, ?);";
	
	public static final String insert_combined =
			"INSERT INTO spy_stats (stat_time, stat_key, string_value, numeric_value) VALUES (?, ?, ?, ?);";
	
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
				PreparedStatement statement = connection.prepareStatement(Database.init);
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
}
