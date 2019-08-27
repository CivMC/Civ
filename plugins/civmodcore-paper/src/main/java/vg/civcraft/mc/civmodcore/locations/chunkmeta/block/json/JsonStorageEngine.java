package vg.civcraft.mc.civmodcore.locations.chunkmeta.block.json;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.StorageEngine;

public class JsonStorageEngine implements StorageEngine {

	private ManagedDatasource db;
	private Logger logger;
	private JsonParser jsonParser;

	public JsonStorageEngine(ManagedDatasource db, Logger logger) {
		this.db = db;
		this.logger = logger;
		this.jsonParser = new JsonParser();
	}

	public void deleteChunkData(int pluginID, int worldID, int x, int z) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement deleteChunk = insertConn.prepareStatement(
						"delete from cmc_chunk_data where x = ? and z = ? and world_id = ? and plugin_id = ?;")) {
			deleteChunk.setInt(1, x);
			deleteChunk.setInt(2, z);
			deleteChunk.setInt(3, worldID);
			deleteChunk.setInt(4, pluginID);
			deleteChunk.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to delete chunk data", e);
		}
	}

	public void insertChunkData(int pluginID, int worldID, int x, int z, JsonObject serializedChunk) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement insertChunk = insertConn.prepareStatement(
						"insert into cmc_chunk_data (x, z, world_id, plugin_id, data) values(?,?,?,?,?)")) {
			insertChunk.setInt(1, x);
			insertChunk.setInt(2, z);
			insertChunk.setInt(3, worldID);
			insertChunk.setInt(4, pluginID);
			insertChunk.setString(5, serializedChunk.toString());
			insertChunk.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to insert chunk data", e);
		}
	}

	public JsonObject loadChunkData(int pluginID, int worldID, int x, int z) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement getData = insertConn.prepareStatement(
						"select ccd.data from cmc_chunk_data where x = ? and z = ? and world_id = ? and plugin_id = ?;")) {
			getData.setInt(1, x);
			getData.setInt(2, z);
			getData.setInt(3, worldID);
			getData.setInt(4, pluginID);
			try (ResultSet rs = getData.executeQuery()) {
				if (rs.next()) {
					return (JsonObject) jsonParser.parse(rs.getString(1));
				}
				return null;
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to load chunk data", e);
			// we want to escalate this, this is really bad
			throw new IllegalStateException("Failed to load chunk data");
		}
	}

	public void updateChunkData(int pluginID, int worldID, int x, int z, JsonObject serializedChunk) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement updateChunk = insertConn.prepareStatement(
						"update cmc_chunk_data set data = ? where x = ? and z = ? and world_id = ? and plugin_id = ?;")) {
			updateChunk.setString(1, serializedChunk.toString());
			updateChunk.setInt(2, x);
			updateChunk.setInt(3, z);
			updateChunk.setInt(4, worldID);
			updateChunk.setInt(5, pluginID);
			updateChunk.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to update chunk data", e);
		}
	}

}
