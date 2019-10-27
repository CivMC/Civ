package com.untamedears.jukealert.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.SnitchManager;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.SnitchFactory;
import com.untamedears.jukealert.model.SnitchTypeManager;
import com.untamedears.jukealert.model.actions.LoggedActionFactory;
import com.untamedears.jukealert.model.actions.LoggedActionPersistence;
import com.untamedears.jukealert.model.actions.abstr.LoggableAction;
import com.untamedears.jukealert.model.appender.AbstractSnitchAppender;

import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.ChunkCoord;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.GlobalChunkMetaManager;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedBlockChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableStorageEngine;

public class JukeAlertDAO extends TableStorageEngine<Snitch> {

	public JukeAlertDAO(Logger logger, ManagedDatasource db) {
		super(logger, db);
	}

	@Override
	public void registerMigrations() {
		// TODO convert old data
		/*
		 * db.registerMigration(1, false, () -> { return true; }, "");
		 */
		db.registerMigration(2, false,
				"create table if not exists ja_snitches (id int not null auto_increment primary key, group_id int, "
						+ "type_id int not null, chunk_x int not null, chunk_z int not null, x int not null, y int not null, z int not null, "
						+ "world_id int not null, name varchar(255),"
						+ "index snitchChunkLookUp(chunk_x, chunk_z, world_id), "
						+ "index snitchLocLookUp(x,y,z, world_id), unique uniqueLoc (world_id, x, y ,z));",
				"create table if not exists ja_snitch_actions(id int not null auto_increment primary key, name varchar(255) not null,"
						+ "constraint unique_name unique(name));",
				"create table if not exists ja_snitch_entries (id int not null auto_increment primary key, "
						+ "snitch_id int, type_id int references ja_snitch_actions(id), "
						+ "uuid char(36) not null, x int not null, y int not null, z int not null, creation_time timestamp not null,"
						+ "victim varchar(255), index `snitch_action_index`(snitch_id));",
				"create table if not exists ja_snitch_refresh (id int primary key references ja_snitches(id) on delete cascade,"
						+ "last_refresh timestamp not null)",
				"create table if not exists ja_snitch_lever (id int primary key references ja_snitches(id) on delete cascade,"
						+ "toggle_lever bool not null)");
	}

	@Override
	public void insert(Snitch snitch, ChunkCoord coord) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement insertSnitch = insertConn.prepareStatement(
						"insert into ja_snitches (group_id, type_id, x, y , z, world_id, chunk_x, chunk_z, name) "
								+ "values(?,?, ?,?,?, ?,?,?, ?);",
						Statement.RETURN_GENERATED_KEYS)) {
			int groupId = snitch.getGroup() == null ? -1 : snitch.getGroup().getGroupId();
			insertSnitch.setInt(1, groupId);
			insertSnitch.setInt(2, snitch.getType().getID());
			insertSnitch.setInt(3, snitch.getLocation().getBlockX());
			insertSnitch.setInt(4, snitch.getLocation().getBlockY());
			insertSnitch.setInt(5, snitch.getLocation().getBlockZ());
			insertSnitch.setInt(6, coord.getWorldID());
			insertSnitch.setInt(7, coord.getX());
			insertSnitch.setInt(8, coord.getZ());
			insertSnitch.setString(9, snitch.getName());
			insertSnitch.execute();
			try (ResultSet rs = insertSnitch.getGeneratedKeys()) {
				if (!rs.next()) {
					throw new IllegalStateException(
							"Inserting snitch at " + snitch.getLocation() + " did not generate an id");
				}
				snitch.setId(rs.getInt(1));
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to insert new snitch: ", e);
		}
		snitch.persistAppenders();
	}

	@Override
	public void update(Snitch snitch, ChunkCoord coord) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement updateSnitch = insertConn
						.prepareStatement("update ja_snitches set name = ?, group_id = ? where id = ?;")) {
			int groupId = snitch.getGroup() == null ? -1 : snitch.getGroup().getGroupId();
			updateSnitch.setString(1, snitch.getName());
			updateSnitch.setInt(2, groupId);
			if (snitch.getId() == -1) {
				throw new IllegalStateException("Snitch id can not be null during update");
			}
			updateSnitch.setInt(3, snitch.getId());
			updateSnitch.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to update snitch: ", e);
		}
		snitch.persistAppenders();
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
		SnitchManager snitchMan = JukeAlert.getInstance().getSnitchManager();
		try (Connection insertConn = db.getConnection();
				PreparedStatement selectSnitch = insertConn
						.prepareStatement("select x, y, z, type_id, group_id, name, id from ja_snitches "
								+ "where chunk_x = ? and chunk_z = ? and world_id = ?;");) {
			selectSnitch.setInt(1, chunkData.getChunkCoord().getX());
			selectSnitch.setInt(2, chunkData.getChunkCoord().getZ());
			selectSnitch.setShort(3, chunkData.getChunkCoord().getWorldID());
			try (ResultSet rs = selectSnitch.executeQuery()) {
				while (rs.next()) {
					int x = rs.getInt(1);
					int y = rs.getInt(2);
					int z = rs.getInt(3);
					Location location = new Location(world, x, y, z);
					int typeID = rs.getInt(4);
					SnitchFactory type = configMan.getConfig(typeID);
					if (type == null) {
						logger.log(Level.SEVERE, "Failed to load snitch with type id " + typeID);
						continue;
					}
					int groupID = rs.getInt(5);
					String name = rs.getString(6);
					int id = rs.getInt(7);
					Snitch snitch = type.create(id, location, name, groupID, false);
					insertFunction.accept(snitch);
					snitchMan.addSnitchToQuadTree(snitch);
					snitch.applyToAppenders(AbstractSnitchAppender::postSetup);
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to load snitch from db: ", e);
		}
	}

	public int getOrCreateActionID(String name) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement selectId = insertConn
						.prepareStatement("select id from ja_snitch_actions where name = ?;")) {
			selectId.setString(1, name);
			try (ResultSet rs = selectId.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to check for existence of action in db: " + e);
			return -1;
		}
		try (Connection insertConn = db.getConnection();
				PreparedStatement insertAction = insertConn.prepareStatement(
						"insert into ja_snitch_actions (name) values(?);", Statement.RETURN_GENERATED_KEYS);) {
			insertAction.setString(1, name);
			insertAction.execute();
			try (ResultSet rs = insertAction.getGeneratedKeys()) {
				if (!rs.next()) {
					logger.info("Failed to insert plugin");
					return -1;
				}
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to insert action into db:", e);
			return -1;
		}
	}

	public List<LoggableAction> loadLogs(Snitch snitch) {
		int id = snitch.getId();
		if (id == -1) {
			throw new IllegalArgumentException("Id for loading logs can not be null");
		}
		List<LoggableAction> result = new ArrayList<>();
		LoggedActionFactory factory = JukeAlert.getInstance().getLoggedActionFactory();
		try (Connection insertConn = db.getConnection();
				PreparedStatement loadActions = insertConn.prepareStatement(
						"select jsa.name, jse.uuid, jse.x, jse.y, jse.z, jse.creation_time, jse.victim"
								+ " from ja_snitch_entries jse inner join ja_snitch_actions jsa on "
								+ "jse.type_id = jsa.id where snitch_id = ?;");) {
			loadActions.setInt(1, id);
			try (ResultSet rs = loadActions.executeQuery()) {
				while (rs.next()) {
					String identifier = rs.getString(1);
					UUID uuid = UUID.fromString(rs.getString(2));
					int x = rs.getInt(3);
					int y = rs.getInt(4);
					int z = rs.getInt(5);
					long time = rs.getTimestamp(6).getTime();
					String victim = rs.getString(7);
					Location loc = new Location(snitch.getLocation().getWorld(), x, y, z);
					LoggableAction action = factory.produce(snitch, identifier, uuid, loc, time, victim);
					if (action != null) {
						result.add(action);
					}
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to load snitch logs from db:", e);
			return new ArrayList<>();
		}
		return result;
	}

	public void deleteLog(LoggableAction log) {
		if (log.getID() == -1) {
			return;
		}
		try (Connection insertConn = db.getConnection();
				PreparedStatement deleteLog = insertConn
						.prepareStatement("delete from ja_snitch_entries where id = ?;")) {
			deleteLog.setInt(1, log.getID());
			deleteLog.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to delete snitch log", e);
		}
	}

	public int insertLog(int typeID, Snitch snitch, LoggedActionPersistence data) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement insertSnitch = insertConn.prepareStatement(
						"insert into ja_snitch_entries (snitch_id, type_id, uuid, x, y , z, creation_time,"
								+ "victim) values(?,?,?, ?,?,?, ?,?);",
						Statement.RETURN_GENERATED_KEYS)) {
			insertSnitch.setInt(1, snitch.getId());
			insertSnitch.setInt(2, typeID);
			insertSnitch.setString(3, data.getPlayer().toString());
			insertSnitch.setInt(4, data.getX());
			insertSnitch.setInt(5, data.getY());
			insertSnitch.setInt(6, data.getZ());
			insertSnitch.setTimestamp(7, new Timestamp(data.getTime()));
			insertSnitch.setString(8, data.getVictim());
			insertSnitch.execute();
			try (ResultSet rs = insertSnitch.getGeneratedKeys()) {
				if (!rs.next()) {
					logger.severe("Failed to insert snitch log, no key retrieved");
				} else {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to insert new snitch log: ", e);
		}
		return -1;
	}

	public void setRefreshTimer(int snitchID, long timer) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement setTimer = insertConn.prepareStatement(
						"insert into ja_snitch_refresh (id, last_refresh) values(?,?) on duplicate key update last_refresh = ?;")) {
			setTimer.setInt(1, snitchID);
			setTimer.setTimestamp(2, new Timestamp(timer));
			setTimer.setTimestamp(3, new Timestamp(timer));
			setTimer.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to update refresh timer", e);
		}
	}

	public void setToggleLever(int snitchID, boolean toggle) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement setTimer = insertConn.prepareStatement(
						"insert into ja_snitch_lever (id, toggle_lever) values(?,?) on duplicate key update toggle_lever = ?;")) {
			setTimer.setInt(1, snitchID);
			setTimer.setBoolean(2, toggle);
			setTimer.setBoolean(3, toggle);
			setTimer.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to update toggle lever", e);
		}
	}

	public boolean getToggleLever(int snitchID) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement selectId = insertConn
						.prepareStatement("select toggle_lever from ja_snitch_lever where id = ?;")) {
			selectId.setInt(1, snitchID);
			try (ResultSet rs = selectId.executeQuery()) {
				if (rs.next()) {
					return rs.getBoolean(1);
				}
				return false;
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to retrieve toggle lever", e);
			return false;
		}
	}

	public long getRefreshTimer(int snitchID) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement selectId = insertConn
						.prepareStatement("select last_refresh from ja_snitch_refresh where id = ?;")) {
			selectId.setInt(1, snitchID);
			try (ResultSet rs = selectId.executeQuery()) {
				if (rs.next()) {
					return rs.getTimestamp(1).getTime();
				}
				return -1;
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to retrieve refresh timer", e);
			return -1;
		}
	}

	public List<Snitch> loadSnitchesByGroupID(Collection<Integer> groupIDs) {
		SnitchTypeManager configMan = JukeAlert.getInstance().getSnitchConfigManager();
		GlobalChunkMetaManager gcmm = CivModCorePlugin.getInstance().getChunkMetaManager();
		List<Snitch> result = new ArrayList<>();
		try (Connection insertConn = db.getConnection()) {
			for (int groupID : groupIDs) {
				try (PreparedStatement selectSnitch = insertConn.prepareStatement(
						"select x, y, z, world_id, type_id, name, id from ja_snitches " + "where group_id = ?;");) {
					selectSnitch.setInt(1, groupID);
					try (ResultSet rs = selectSnitch.executeQuery()) {
						while (rs.next()) {
							int x = rs.getInt(1);
							int y = rs.getInt(2);
							int z = rs.getInt(3);
							int worldID = rs.getInt(4);
							World world = gcmm.getWorldByInternalID((short) worldID);
							if (world == null) {
								logger.log(Level.SEVERE, "Failed to load snitch with world id " + worldID);
								continue;
							}
							Location location = new Location(world, x, y, z);
							int typeID = rs.getInt(5);
							SnitchFactory type = configMan.getConfig(typeID);
							if (type == null) {
								logger.log(Level.SEVERE, "Failed to load snitch with type id " + typeID);
								continue;
							}
							String name = rs.getString(6);
							int id = rs.getInt(7);
							Snitch snitch = type.create(id, location, name, groupID, false);
							result.add(snitch);
						}
					}
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to load snitch from db based on group id: ", e);
		}
		return result;
	}

}
