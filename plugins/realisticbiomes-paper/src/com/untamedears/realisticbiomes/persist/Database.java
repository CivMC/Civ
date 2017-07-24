package com.untamedears.realisticbiomes.persist;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import com.untamedears.realisticbiomes.PersistConfig;
import com.untamedears.realisticbiomes.RealisticBiomes;
import com.zaxxer.hikari.HikariConfig;

/**
 * Wrapper for Connection Pool, and holder for instance-static strings.
 * 
 * @author ProgrammerDan
 */
public class Database {

	private HikariDataSource datasource;
	
	public Database(PersistConfig baseconfig) {
		
		initStatements(baseconfig);
		
		if (baseconfig.user != null && baseconfig.host != null && 
				baseconfig.port != null && baseconfig.databaseName != null) {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl("jdbc:mysql://" + baseconfig.host + ":" + baseconfig.port + "/" + baseconfig.databaseName);
			// TODO make these config'd
			config.setConnectionTimeout(1000l);
			config.setIdleTimeout(600000l);
			config.setMaxLifetime(7200000l);
			// END TODO
			config.setMaximumPoolSize(baseconfig.poolSize);
			config.setUsername(baseconfig.user);
			if (baseconfig.password != null) {
				config.setPassword(baseconfig.password);
			}
			this.datasource = new HikariDataSource(config);

			RealisticBiomes.doLog(Level.FINER, "creating chunk table (if necessary) with prepared statement:" + Database.makeTableChunk);

			try (Connection connection = getConnection();
				PreparedStatement statement = connection.prepareStatement(Database.makeTableChunk);) {
				statement.execute();
				statement.close();
			} catch (SQLException se) {
				RealisticBiomes.doLog(Level.SEVERE, "Unable to initialize chunk table in Database!", se);
				this.datasource = null;
				return;
			}

			try (Connection connection = getConnection();
				PreparedStatement statement = connection.prepareStatement(Database.makeTablePlant);) {
				statement.execute();
			} catch (SQLException se) {
				RealisticBiomes.doLog(Level.SEVERE, "Unable to initialize plant table in Database!", se);
				this.datasource = null;
				return;
			}

			/* MIGRATIONS */
			
			// update database schema: try and catch
			try (Connection connection = getConnection();
				PreparedStatement upgradeTablePlant = connection.prepareStatement(Database.migration0001);){
				upgradeTablePlant.execute();
			} catch (SQLException e) {
				RealisticBiomes.LOG.info("Could not update table - ignore if already updated. Error code: " + e.getErrorCode() + ", error message: " + e.getMessage());
			}

		} else {
			this.datasource = null;
			RealisticBiomes.doLog(Level.SEVERE, "Database not configured and is unavaiable");
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

	public static String deleteOldDataStmt = null;
	public static String deleteChunkStmt = null;
	public static String addChunkStmt = null;
	public static String updatePlantStmt = null;
	public static String addPlantStmt = null;
	public static String deleteOldPlantsStmt = null;
	public static String loadPlantsStmt = null;
	public static String makeTableChunk = null;
	public static String makeTablePlant = null;
	public static String selectAllFromChunk = null;
	
	// MIGRATIONS -- TODO: convert to civmodcore migrations
	public static String migration0001 = null;

	
	private void initStatements(PersistConfig config) {

		deleteOldDataStmt = String.format("DELETE FROM %s_plant WHERE chunkid = ?", config.prefix);
			
		addChunkStmt = String.format("INSERT INTO %s_chunk (w, x, z) VALUES (?, ?, ?)", config.prefix);
			
		addPlantStmt = String.format("INSERT INTO %s_plant (chunkid, w, x, y, z, date, growth, fruitGrowth) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", config.prefix);

		// don't need for now,...maybe later?
		//updatePlantStmt = String.format("UPDATE %s_plant SET date = ?, growth = ? where chunkid = ?", config.prefix);
		deleteOldPlantsStmt = String.format("DELETE FROM %s_plant WHERE chunkid = ?", config.prefix);		
		
		loadPlantsStmt = String.format("SELECT w, x, y, z, date, growth, fruitGrowth FROM %s_plant WHERE chunkid = ?", config.prefix);
		
		makeTableChunk = String.format("CREATE TABLE IF NOT EXISTS %s_chunk " +
				"(id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
				"w INTEGER, x INTEGER, z INTEGER," +
				"INDEX chunk_coords_idx (w, x, z)) " +
				"ENGINE INNODB", config.prefix);

		// we need InnoDB storage engine or else we can't do foreign keys!
		makeTablePlant = String.format("CREATE TABLE IF NOT EXISTS %s_plant" +
				"(chunkId BIGINT, w INTEGER, x INTEGER, y INTEGER, z INTEGER, date INTEGER UNSIGNED, growth REAL, fruitGrowth REAL, " +
				"INDEX plant_chunk_idx (chunkId), " +
				"CONSTRAINT chunkIdConstraint FOREIGN KEY (chunkId) REFERENCES %s_chunk (id))" +
				"ENGINE INNODB", config.prefix, config.prefix);

		selectAllFromChunk = String.format("SELECT id, w, x, z FROM %s_chunk", config.prefix);
	
		migration0001 = String.format("ALTER TABLE %s_plant ADD fruitGrowth REAL AFTER growth", config.prefix);
	}
}
