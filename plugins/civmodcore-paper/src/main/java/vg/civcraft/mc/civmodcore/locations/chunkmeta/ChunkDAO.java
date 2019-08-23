package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class ChunkDAO {

	private ManagedDatasource db;
	private Logger logger;
	private ChunkMetaFactory dataFactory;

	public ChunkDAO(ManagedDatasource db, CivModCorePlugin plugin) {
		this.db = db;
		this.logger = plugin.getLogger();
		this.dataFactory = new ChunkMetaFactory();
	}

	void deleteChunkData(int pluginID, int worldID, int x, int z) {

	}

	public ChunkMetaFactory getChunkMetaFactory() {
		return dataFactory;
	}

	int getOrCreatePluginID(JavaPlugin plugin) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement insertPlugin = insertConn
						.prepareStatement("select id from cmc_plugins where uuid = ?;")) {
			insertPlugin.setString(1, plugin.getName());
			try (ResultSet rs = insertPlugin.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			logger.severe("Failed to check for existence of plugin in db: " + e.toString());
			return -1;
		}
		try (Connection insertConn = db.getConnection();
				PreparedStatement insertPlugin = insertConn.prepareStatement(
						"insert into cmc_plugins (name) values(?);", Statement.RETURN_GENERATED_KEYS);) {
			insertPlugin.setString(1, plugin.getName());
			insertPlugin.execute();
			try (ResultSet rs = insertPlugin.getGeneratedKeys()) {
				if (!rs.next()) {
					logger.info("Failed to insert plugin");
					return -1;
				}
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.severe("Failed to insert plugin into db: " + e.toString());
			return -1;
		}
	}

	int getOrCreateWorldID(World world) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement insertWorld = insertConn
						.prepareStatement("select id from cmc_worlds where uuid = ?;")) {
			insertWorld.setString(1, world.getUID().toString());
			try (ResultSet rs = insertWorld.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			logger.severe("Failed to check for existence of world in db: " + e.toString());
			return -1;
		}
		try (Connection insertConn = db.getConnection();
				PreparedStatement insertWorld = insertConn.prepareStatement(
						"insert into cmc_worlds (uuid, name) values(?,?);", Statement.RETURN_GENERATED_KEYS);) {
			insertWorld.setString(1, world.getUID().toString());
			insertWorld.setString(2, world.getName());
			insertWorld.execute();
			try (ResultSet rs = insertWorld.getGeneratedKeys()) {
				if (!rs.next()) {
					logger.info("Failed to insert world");
					return -1;
				}
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.severe("Failed to insert world into db: " + e.toString());
			return -1;
		}
	}

	void insertChunkData(int pluginID, int worldID, int x, int z, ChunkMeta meta) {

	}

	void loadChunkData(int worldID, ChunkCoord coord) {
		synchronized (coord) {
			if (coord.isFullyLoaded()) {
				return;
			}
			try (Connection insertConn = db.getConnection();
					PreparedStatement getData = insertConn.prepareStatement(
							"select ccd.data, cp.id from cmc_chunk_data ccd inner join cmc_plugins cp on ccd.plugin_id = cp.id"
									+ " where ccd.x = ? and ccd.z = ? and ccd.world_id = ?;")) {
				getData.setInt(1, coord.getX());
				getData.setInt(2, coord.getZ());
				getData.setInt(3, worldID);
				try (ResultSet rs = getData.executeQuery()) {
					while (rs.next()) {
						String rawJsonString = rs.getString(1);
						int pluginID = rs.getInt(2);
						ChunkMeta meta = dataFactory.deserialize(rawJsonString, pluginID);
						coord.addChunkMeta(meta);
					}
				}
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "Failed to load chunk data", e);
				// we want to escalate this, this is really bad
				throw new IllegalStateException("Failed to load chunk data");
			}
			coord.setFullyLoaded();
		}
	}

	private void registerMigrations() {
		db.registerMigration(1, false,
				"create table cmc_worlds (id int not null auto_increment primary key, uuid char(36) not null, "
						+ "name text not null, constraint uniqueUuid unique(uuid));",
				"create table cmc_plugins (id int not null auto_increment primary key, name text not null, "
						+ "constraint uniqueName unique(name));",
				"create table cmc_chunk_data (x int not null, z int not null, world_id int not null references cmc_worlds(id), "
						+ "plugin_id int not null references cmc_plugins(id), data text not null,"
						+ "primary key cmc_chunk_lookup(world_id, x, z, plugin_id));");
	}

	void updateChunkData(int pluginID, int worldID, int x, int z, ChunkMeta meta) {

	}

	public boolean updateDatabase() {
		registerMigrations();
		return db.updateDatabase();
	}

}
