package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class ChunkDAO {

	private ManagedDatasource db;
	private Logger logger;

	public ChunkDAO(ManagedDatasource db, CivModCorePlugin plugin) {
		this.db = db;
		this.logger = plugin.getLogger();
	}

	public int getOrCreatePluginID(JavaPlugin plugin) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement insertPlugin = insertConn
						.prepareStatement("select id from cmc_plugins where name = ?;")) {
			insertPlugin.setString(1, plugin.getName());
			try (ResultSet rs = insertPlugin.executeQuery()) {
				if (rs.next()) {
					return rs.getShort(1);
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
				return rs.getShort(1);
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
					return rs.getShort(1);
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
				return rs.getShort(1);
			}
		} catch (SQLException e) {
			logger.severe("Failed to insert world into db: " + e.toString());
			return -1;
		}
	}

	private void registerMigrations() {
		db.registerMigration(1, false,
				"create table if not exists cmc_worlds (id smallint unsigned not null auto_increment primary key, uuid char(36) not null, "
						+ "name text not null, constraint uniqueUuid unique(uuid));",
				"create table if not exists cmc_plugins (id smallint unsigned not null auto_increment primary key, name varchar(255) not null, "
						+ "constraint uniqueName unique(name));",
				"create table if not exists cmc_chunk_data (x int not null, z int not null, world_id smallint unsigned not null references cmc_worlds(id), "
						+ "plugin_id smallint unsigned not null references cmc_plugins(id), data text not null,"
						+ "primary key cmc_chunk_lookup(world_id, x, z, plugin_id));");
	}

	public boolean updateDatabase() {
		registerMigrations();
		return db.updateDatabase();
	}

}
