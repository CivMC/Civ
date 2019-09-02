package com.untamedears.JukeAlert.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.SnitchTypeManager;
import com.untamedears.JukeAlert.model.factory.SnitchConfigFactory;

import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.ChunkCoord;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedBlockChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableStorageEngine;

public class JukeAlertDAO extends TableStorageEngine<Snitch> {

	public JukeAlertDAO(Logger logger, ManagedDatasource db) {
		super(logger, db);
	}

	@Override
	public void registerMigrations() {
		// TODO convert old data
		db.registerMigration(1, false, () -> {
			return true;
		}, "");
		db.registerMigration(2, false,
				"create table if not exists ja_snitches (id int not null auto_increment primary key, group_id int, "
						+ "type_id int not null, chunk_x int not null, chunk_z int not null, x int not null, y int not null, z int not null, "
						+ "world_id int not null, name varchar(255), last_refresh timestamp not null, toggle_lever bool not null,"
						+ "index snitchChunkLookUp(chunk_x, chunk_z, world_id), "
						+ "index snitchLocLookUp(x,y,z, world_id), unique uniqueLoc (world_id, x, y ,z));",
				"create table if not exists ja_snitch_entries_player (id int not null auto_increment primary key, uuid char(36), "
						+ "snitch_id int references ja_snitches(id));");
	}

	@Override
	public void insert(Snitch snitch, ChunkCoord coord) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement insertSnitch = insertConn.prepareStatement(
						"insert into ja_snitches (group_id, type_id, x, y , z, world_id, chunk_x, chunk_z, name, last_refresh,toggle_lever) "
								+ "(?,?, ?,?,?, ?,?,?, ?,?,?);",
						Statement.RETURN_GENERATED_KEYS)) {
			int groupId = snitch.getGroup() == null ? -1 : snitch.getGroup().getGroupId();
			insertSnitch.setInt(1, groupId);
			insertSnitch.setInt(2, snitch.getTypeID());
			insertSnitch.setInt(3, snitch.getLocation().getBlockX());
			insertSnitch.setInt(4, snitch.getLocation().getBlockY());
			insertSnitch.setInt(5, snitch.getLocation().getBlockZ());
			insertSnitch.setInt(6, coord.getWorldID());
			insertSnitch.setInt(7, coord.getX());
			insertSnitch.setInt(8, coord.getZ());
			insertSnitch.setString(9, snitch.getName());
			insertSnitch.setTimestamp(10, new Timestamp(snitch.getLastRefresh()));
			insertSnitch.setBoolean(11, snitch.shouldToggleLevers());
			try (ResultSet rs = insertSnitch.executeQuery()) {
				if (!rs.next()) {
					throw new IllegalStateException(
							"Inserting snitch at " + snitch.getLocation() + " did not generate an id");
				}
				snitch.setId(rs.getInt(1));
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to insert new snitch: ", e);
		}
	}

	@Override
	public void update(Snitch snitch, ChunkCoord coord) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement updateSnitch = insertConn.prepareStatement(
						"update ja_snitches set name = ?, last_refresh = ?, group_id = ?, toggle_lever = ? where id = ?;")) {
			int groupId = snitch.getGroup() == null ? -1 : snitch.getGroup().getGroupId();
			updateSnitch.setString(1, snitch.getName());
			updateSnitch.setTimestamp(2, new Timestamp(snitch.getLastRefresh()));
			updateSnitch.setInt(3, groupId);
			updateSnitch.setBoolean(4, snitch.shouldToggleLevers());
			if (snitch.getId() == -1) {
				throw new IllegalStateException("Snitch id can not be null during update");
			}
			updateSnitch.setInt(4, snitch.getId());
			updateSnitch.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to update snitch: ", e);
		}
	}

	@Override
	public void delete(Snitch snitch, ChunkCoord coord) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement deleteSnitch = insertConn.prepareStatement("delete from ja_snitches where id = ?;")) {
			deleteSnitch.setInt(1, snitch.getId());
			deleteSnitch.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to delete snitch: ", e);
		}
	}

	@Override
	public void fill(TableBasedBlockChunkMeta<Snitch> chunkData, Consumer<Snitch> insertFunction) {
		World world = chunkData.getChunkCoord().getWorld();
		SnitchTypeManager configMan = JukeAlert.getInstance().getSnitchConfigManager();
		try (Connection insertConn = db.getConnection();
				PreparedStatement selectSnitch = insertConn.prepareStatement(
						"select x, y, z, type_id, group_id, name, last_refresh, toggle_lever from ja_snitches "
						+ "where chunk_x = ? and chunk_z = ? and world_id = ?;");) {
			selectSnitch.setInt(1, chunkData.getChunkCoord().getX());
			selectSnitch.setInt(2, chunkData.getChunkCoord().getZ());
			selectSnitch.setShort(3, (short) chunkData.getChunkCoord().getWorldID());
			try (ResultSet rs = selectSnitch.executeQuery()) {
				while (rs.next()) {
					int x = rs.getInt(1);
					int y = rs.getInt(2);
					int z = rs.getInt(3);
					Location location = new Location(world, x, y, z);
					int typeID = rs.getInt(4);
					SnitchConfigFactory type = configMan.getConfig(typeID);
					if (type == null) {
						logger.log(Level.SEVERE, "Failed to load snitch with type id " + typeID);
						continue;
					}
					int groupID = rs.getInt(5);
					String name = rs.getString(6);
					long lastRefresh = rs.getTimestamp(7).getTime();
					boolean triggerLevers = rs.getBoolean(8);
					Snitch snitch = type.recreate(location, name, groupID, lastRefresh, triggerLevers);
					insertFunction.accept(snitch);
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to load snitch from db: ", e);
		}

	}

}
