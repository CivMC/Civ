package com.untamedears.realisticbiomes.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


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
	public static PersistConfig curConfig;
	
	public static PreparedStatement loadPlantsStmt = null;
	
	public static void init(PersistConfig config) {
		try {
			curConfig = config;
			
			initWrite();
			initRead();
		} catch (SQLException e) {
			throw new DataSourceException("Failed to create the prepared statements in ChunkWriter", e);
		}
	}
	
	public static void initWrite() throws SQLException{
		Connection writeConnection = RealisticBiomes.plugin.getPlantManager().getWriteConnection();
		deleteOldDataStmt = writeConnection.prepareStatement(String.format("DELETE FROM %s_plant WHERE chunkid = ?", curConfig.prefix));
		
		addChunkStmt = writeConnection.prepareStatement(String.format("INSERT INTO %s_chunk (w, x, z) VALUES (?, ?, ?)", curConfig.prefix));
		getLastChunkIdStmt = writeConnection.prepareStatement("SELECT LAST_INSERT_ID()");	
		
		addPlantStmt = writeConnection.prepareStatement(String.format("INSERT INTO %s_plant (chunkid, w, x, y, z, date, growth, fruitGrowth) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", curConfig.prefix));
		// don't need for now,...maybe later?
		//updatePlantStmt = writeConnection.prepareStatement(String.format("UPDATE %s_plant SET date = ?, growth = ? where chunkid = ?", curConfig.prefix));
		deleteOldPlantsStmt = writeConnection.prepareStatement(String.format("DELETE FROM %s_plant WHERE chunkid = ?", curConfig.prefix));		
	}
	
	public static void initRead() throws SQLException{
		Connection readConnection = RealisticBiomes.plugin.getPlantManager().getReadConnection();
		loadPlantsStmt = readConnection.prepareStatement(String
				.format("SELECT w, x, y, z, date, growth, fruitGrowth FROM %s_plant WHERE chunkid = ?",
						curConfig.prefix));		
	}

}
