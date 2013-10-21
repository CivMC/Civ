package com.untamedears.realisticbiomes.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebeaninternal.server.lib.sql.DataSourceException;
import com.untamedears.realisticbiomes.PersistConfig;
import com.untamedears.realisticbiomes.RealisticBiomes;

/**
 * basically a container class that holds most of the prepared statements that we will be using often
 * use the static methods to get the prepared statement you want, which makes sure that the prepared statement has its 
 * params cleared
 * 
 * @author Mark
 *
 */
public class ChunkWriter {
	public static PreparedStatement deleteOldDataStmt = null;
	public static PreparedStatement deleteChunkStmt = null;
	public static PreparedStatement addChunkStmt = null;
	public static PreparedStatement updatePlantStmt = null;
	public static PreparedStatement getLastChunkIdStmt = null;
	public static PreparedStatement addPlantStmt = null;
	public static PreparedStatement deleteOldPlantsStmt = null;
	public static Connection writeConnection;
	public static Connection readConnection;
	
	public static PreparedStatement loadPlantsStmt = null;
	
	public ChunkWriter(Connection writeConn, Connection readConn,  PersistConfig config) {

		try {
			
			writeConnection = writeConn;
			deleteOldDataStmt = writeConn.prepareStatement(String.format("DELETE FROM %s_plant WHERE chunkid = ?", config.prefix));
			
			addChunkStmt = writeConn.prepareStatement(String.format("INSERT INTO %s_chunk (w, x, z) VALUES (?, ?, ?)", config.prefix));
			getLastChunkIdStmt = writeConn.prepareStatement("SELECT LAST_INSERT_ID()");	
			
			addPlantStmt = writeConn.prepareStatement(String.format("INSERT INTO %s_plant (chunkid, w, x, y, z, date, growth) VALUES (?, ?, ?, ?, ?, ?, ?)", config.prefix));
			// don't need for now,...maybe later?
			//updatePlantStmt = writeConn.prepareStatement(String.format("UPDATE %s_plant SET date = ?, growth = ? where chunkid = ?", config.prefix));
			deleteOldPlantsStmt = writeConn.prepareStatement(String.format("DELETE FROM %s_plant WHERE chunkid = ?", config.prefix));

			loadPlantsStmt = readConn.prepareStatement(String
								.format("SELECT w, x, y, z, date, growth FROM %s_plant WHERE chunkid = ?",
										config.prefix));


		} catch (SQLException e) {
			throw new DataSourceException("Failed to create the prepared statements in ChunkWriter", e);
		}
	}
	

	
	
	
	
}
