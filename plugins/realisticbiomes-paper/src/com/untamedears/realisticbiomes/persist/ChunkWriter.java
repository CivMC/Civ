package com.untamedears.realisticbiomes.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChunkWriter {
	public static PreparedStatement deleteOldDataStmt = null;
	public static PreparedStatement deleteChunkStmt = null;
	public static PreparedStatement addChunkStmt = null;
	public static PreparedStatement savePlantsStmt = null;
	public static PreparedStatement getLastChunkIdStmt = null;

	public ChunkWriter(Connection writeConn) {

		try {
			deleteOldDataStmt = writeConn.prepareStatement("DELETE FROM plant WHERE chunkid = ?1");
			
			addChunkStmt = writeConn.prepareStatement("INSERT INTO chunk (w, x, z) VALUES (?, ?, ?)");
			getLastChunkIdStmt = writeConn.prepareStatement("SELECT last_insert_rowid()");	
			
			savePlantsStmt = writeConn.prepareStatement("INSERT INTO plant (chunkid, w, x, y, z, date, growth) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7)");
			
			deleteChunkStmt = writeConn.prepareStatement("DELETE FROM chunk WHERE id = ?1");
		} catch (SQLException e) {
			throw new DataSourceException("Failed to create the prepared statements in ChunkWriter", e);
		}
	}
}
