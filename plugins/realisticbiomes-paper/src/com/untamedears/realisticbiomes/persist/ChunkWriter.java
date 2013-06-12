package com.untamedears.realisticbiomes.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.avaje.ebeaninternal.server.lib.sql.DataSourceException;
import com.untamedears.realisticbiomes.PersistConfig;

public class ChunkWriter {
	public static PreparedStatement deleteOldDataStmt = null;
	public static PreparedStatement deleteChunkStmt = null;
	public static PreparedStatement addChunkStmt = null;
	public static PreparedStatement savePlantsStmt = null;
	public static PreparedStatement getLastChunkIdStmt = null;

	
	public ChunkWriter(Connection writeConn, PersistConfig config) {

		try {
			deleteOldDataStmt = writeConn.prepareStatement(String.format("DELETE FROM %s_plant WHERE chunkid = ?1", config.prefix));
			
			addChunkStmt = writeConn.prepareStatement(String.format("INSERT INTO %s_chunk (w, x, z) VALUES (?, ?, ?)", config.prefix));
			getLastChunkIdStmt = writeConn.prepareStatement("SELECT LAST_INSERT_ID()");	
			
			savePlantsStmt = writeConn.prepareStatement(String.format("INSERT INTO %s_plant (chunkid, w, x, y, z, date, growth) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7)", config.prefix));
			
			deleteChunkStmt = writeConn.prepareStatement(String.format("DELETE FROM %s_chunk WHERE id = ?1", config.prefix));
		} catch (SQLException e) {
			throw new DataSourceException("Failed to create the prepared statements in ChunkWriter", e);
		}
	}
}
