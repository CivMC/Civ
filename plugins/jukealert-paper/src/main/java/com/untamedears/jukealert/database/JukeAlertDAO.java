package com.untamedears.jukealert.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.SnitchManager;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.SnitchFactoryType;
import com.untamedears.jukealert.model.SnitchTypeManager;
import com.untamedears.jukealert.model.actions.ActionCacheState;
import com.untamedears.jukealert.model.actions.LoggedActionFactory;
import com.untamedears.jukealert.model.actions.LoggedActionPersistence;
import com.untamedears.jukealert.model.actions.abstr.LoggableAction;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;
import com.untamedears.jukealert.model.appender.AbstractSnitchAppender;

import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.CacheState;
import vg.civcraft.mc.civmodcore.locations.global.GlobalTrackableDAO;
import vg.civcraft.mc.civmodcore.locations.global.WorldIDManager;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class JukeAlertDAO extends GlobalTrackableDAO<Snitch> {

	public JukeAlertDAO(Logger logger, ManagedDatasource db) {
		super(logger, db);
	}

	@Override
	public void registerMigrations() {
		// legacy format
		db.registerMigration(1, false,
				"CREATE TABLE IF NOT EXISTS snitchs (snitch_id int(10) unsigned NOT NULL AUTO_INCREMENT,"
						+ "snitch_world varchar(40) NOT NULL, snitch_name varchar(40) NOT NULL, snitch_x int(10) NOT NULL,"
						+ "snitch_y int(10) NOT NULL, snitch_z int(10) NOT NULL, snitch_group varchar(255) NOT NULL,"
						+ "snitch_cuboid_x int(10) NOT NULL, snitch_cuboid_y int(10) NOT NULL, snitch_cuboid_z int(10) NOT NULL,"
						+ "snitch_should_log tinyint(1) DEFAULT NULL, last_semi_owner_visit_date datetime NOT NULL, "
						+ "allow_triggering_lever bit(1) NOT NULL, soft_delete tinyint(1) NOT NULL DEFAULT '0', "
						+ "PRIMARY KEY (snitch_id), KEY idx_y (snitch_y), KEY idx_last_visit (last_semi_owner_visit_date,snitch_should_log))",
				"CREATE TABLE IF NOT EXISTS snitch_details (snitch_details_id int(10) unsigned NOT NULL AUTO_INCREMENT, "
						+ " snitch_id int(10) unsigned NOT NULL, snitch_log_time datetime DEFAULT NULL, "
						+ "snitch_logged_action tinyint(3) unsigned NOT NULL, snitch_logged_initiated_user varchar(16) NOT NULL, "
						+ "snitch_logged_victim_user varchar(16) DEFAULT NULL, snitch_logged_x int(10) DEFAULT NULL,"
						+ "snitch_logged_Y int(10) DEFAULT NULL, snitch_logged_z int(10) DEFAULT NULL,"
						+ "snitch_logged_materialid smallint(5) unsigned DEFAULT NULL, soft_delete tinyint(1) NOT NULL DEFAULT '0',"
						+ "log_hour mediumint(9) DEFAULT NULL, PRIMARY KEY (snitch_details_id),  KEY idx_snitch_id (snitch_id),"
						+ "KEY idx_log_time (snitch_log_time), KEY idx_log_hour (log_hour), "
						+ "CONSTRAINT fk_snitchs_snitch_id FOREIGN KEY (snitch_id) REFERENCES snitchs (snitch_id) "
						+ "ON DELETE CASCADE ON UPDATE CASCADE)");

		db.registerMigration(2, false, new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				Map<Integer, Integer> oldToNewId = new HashMap<>();
				try (Connection insertConn = db.getConnection();
						PreparedStatement selectSnitches = insertConn.prepareStatement(
								"select snitch_x,snitch_y,snitch_z,snitch_world,snitch_name,snitch_should_log,last_semi_owner_visit_date,"
										+ "snitch_group,allow_triggering_lever,snitch_id from snitchs order by snitch_id asc");
						ResultSet rs = selectSnitches.executeQuery();
						PreparedStatement insertSnitch = insertConn.prepareStatement(
								"insert into ja_snitches (group_id, type_id, x, y , z, world_id, name) "
										+ "values(?,?, ?,?,?, ?, ?);",
								Statement.RETURN_GENERATED_KEYS);) {
					try (PreparedStatement deleteExisting = insertConn.prepareStatement("delete from ja_snitches")) {
						// in case this migration failed before some of the data might already have
						// migrated, which we want to undo
						deleteExisting.execute();
					}

					WorldIDManager worldIdMan = CivModCorePlugin.getInstance().getWorldIdManager();
					while (rs.next()) {
						int x = rs.getInt(1);
						int y = rs.getInt(2);
						int z = rs.getInt(3);
						String worldName = rs.getString(4);
						String name = rs.getString(5);
						boolean logging = rs.getBoolean(6);
						long lastVisit = rs.getTimestamp(7).getTime();
						String groupName = rs.getString(8);
						boolean triggerLever = rs.getBoolean(9);
						int oldId = rs.getInt(10);

						short worldID = worldIdMan.getInternalWorldIdByName(worldName);
						if (worldID == -1) {
							logger.severe("Failed to find world id for world with name " + worldName);
							return false;
						}
						int snitchType = logging ? 1 : 0;
						Group group = GroupManager.getGroup(groupName);
						if (group == null) {
							continue;
						}
						int groupId = group.getGroupId();

						insertSnitch.setInt(1, groupId);
						insertSnitch.setInt(2, snitchType);
						insertSnitch.setInt(3, x);
						insertSnitch.setInt(4, y);
						insertSnitch.setInt(5, z);
						insertSnitch.setShort(6, worldID);
						insertSnitch.setString(7, name);
						insertSnitch.execute();
						try (ResultSet keySet = insertSnitch.getGeneratedKeys()) {
							if (!keySet.next()) {
								logger.severe(
										"Inserting snitch at " + x + " " + y + " " + z + " did not generate an id");
								return false;
							}
							int id = rs.getInt(1);
							setRefreshTimer(id, lastVisit);
							if (triggerLever) {
								setToggleLever(id, true);
							}
							oldToNewId.put(oldId, id);
						}
					}
				}
				try (Connection insertConn = db.getConnection();
						PreparedStatement selectSnitches = insertConn.prepareStatement(
								"select snitch_id, snitch_log_time, snitch_logged_action, snitch_logged_initiated_user, snitch_logged_victim_user,"
										+ "snitch_logged_x, snitch_logged_y, snitch_logged_z from snitch_details order by snitch_details_id asc");
						ResultSet rs = selectSnitches.executeQuery();
						PreparedStatement insertSnitch = insertConn.prepareStatement(
								"insert into ja_snitch_entries (snitch_id, type_id, uuid, x, y , z, creation_time,"
										+ "victim) values(?,?,?, ?,?,?, ?,?);")) {
					try (PreparedStatement deleteExisting = insertConn
							.prepareStatement("delete from ja_snitch_entries")) {
						// in case this migration failed before some of the data might already have
						// migrated, which we want to undo
						deleteExisting.execute();
					}
					int batchCounter = 0;
					while (rs.next()) {
						int oldId = rs.getInt(1);
						long logTime = rs.getTimestamp(2).getTime();
						byte actionType = rs.getByte(3);
						String actor = rs.getString(4);
						String victim = rs.getString(5);
						int x = rs.getInt(6);
						int y = rs.getInt(7);
						int z = rs.getInt(8);

						actor = ChatColor.stripColor(actor);
						UUID actorUUID = NameAPI.getUUID(actor);
						if (actorUUID == null) {
							actorUUID = UUID.fromString("8326bc56-1ed9-40ff-8f24-46bf3e300e51");
						}
						int newSnitchId = oldToNewId.get(oldId);
						switch (actionType) {
						case 0:
							try {
								EntityType.valueOf(victim);
							} catch (IllegalArgumentException e) {
								victim = EntityType.PLAYER.toString();
							}
							break;
						case 1:
						case 2:
							victim = Material.STONE.toString();
							break;
						case 3:
							victim = Material.WATER.toString();
							break;
						case 4:
							victim = Material.WATER_BUCKET.toString();
							break;
						case 6:
						case 8:
							// entirely skip, these shouldn't even exist
							continue;
						case 9:
							victim = Material.CHEST.toString();
							break;
						case 12:
							// no IE support atm, might readd later
							continue;
						}

						insertSnitch.setInt(1, newSnitchId);
						insertSnitch.setInt(2, actionType);
						insertSnitch.setString(3, actorUUID.toString());
						insertSnitch.setInt(4, x);
						insertSnitch.setInt(5, y);
						insertSnitch.setInt(6, z);
						insertSnitch.setTimestamp(7, new Timestamp(logTime));
						insertSnitch.setString(8, victim);
						insertSnitch.addBatch();
						if (batchCounter > 10000) {
							batchCounter = 0;
							insertSnitch.executeBatch();
						}
						batchCounter++;
					}
					insertSnitch.executeBatch();
				}
				return true;
			}
		}, "create table if not exists ja_snitches (id int not null auto_increment primary key, group_id int, "
				+ "type_id int not null, x int not null, y int not null, z int not null, "
				+ "world_id smallint unsigned not null, name varchar(255),"
				+ "index snitchLocLookUp(x,y,z, world_id), unique uniqueLoc (world_id, x, y ,z));",
				"create table if not exists ja_snitch_actions(id int not null auto_increment primary key, name varchar(255) not null,"
						+ "constraint unique_name unique(name));",
				"delete from ja_snitch_actions",
				"create table if not exists ja_snitch_entries (id int not null auto_increment primary key, "
						+ "snitch_id int, type_id int references ja_snitch_actions(id), "
						+ "uuid char(36) not null, x int not null, y int not null, z int not null, creation_time timestamp not null,"
						+ "victim varchar(255), index `snitch_action_index`(snitch_id));",
				"create table if not exists ja_snitch_refresh (id int primary key references ja_snitches(id) on delete cascade,"
						+ "last_refresh timestamp not null)",
				"create table if not exists ja_snitch_lever (id int primary key references ja_snitches(id) on delete cascade,"
						+ "toggle_lever bool not null)",
				"ALTER TABLE ja_snitch_actions AUTO_INCREMENT = 16",
				"insert into ja_snitch_actions(id,name) values(0, 'KILL_MOB'),(1,'BLOCK_PLACE'),(2,'BLOCK_BREAK'),(3,'FILL_BUCKET'),(4,'EMPTY_BUCKET'),"
						+ "(5,'ENTRY'),(7,'IGNITE_BLOCK'),(9,'OPEN_CONTAINER'),(10,'LOGIN'),(11,'LOGOUT'),(13,'DESTROY_VEHICLE'),"
						+ "(14,'MOUNT_ENTITY'),(15,'DISMOUNT_ENTITY')",
				"delete from snitchs using snitchs, snitchs s2 where snitchs.snitch_id < s2.snitch_id "
						+ "and snitchs.snitch_x = s2.snitch_x and snitchs.snitch_y = s2.snitch_y and snitchs.snitch_z = s2.snitch_z and snitchs.snitch_world=s2.snitch_world");
		db.registerMigration(3, false, "delete from ja_snitches where group_id = -1");
	}

	@Override
	public void insert(Snitch snitch) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement insertSnitch = insertConn
						.prepareStatement("insert into ja_snitches (group_id, type_id, x, y , z, world_id, name) "
								+ "values(?,?, ?,?,?, ?, ?);", Statement.RETURN_GENERATED_KEYS)) {
			if (snitch.getGroup() == null) {
				return;
			}
			int groupId = snitch.getGroup().getGroupId();
			insertSnitch.setInt(1, groupId);
			insertSnitch.setInt(2, snitch.getType().getID());
			insertSnitch.setInt(3, snitch.getLocation().getBlockX());
			insertSnitch.setInt(4, snitch.getLocation().getBlockY());
			insertSnitch.setInt(5, snitch.getLocation().getBlockZ());
			insertSnitch.setShort(6, getWorldID(snitch.getLocation()));
			insertSnitch.setString(7, snitch.getName());
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
	public void update(Snitch snitch) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement updateSnitch = insertConn
						.prepareStatement("update ja_snitches set name = ?, group_id = ? where id = ?;")) {
			int groupId = snitch.getGroup() == null ? -1 : snitch.getGroup().getGroupId();
			if (groupId == -1) {
				delete(snitch);
				snitch.setCacheState(CacheState.DELETED);
				return;
			}
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
	public void delete(Snitch snitch) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement deleteSnitch = insertConn.prepareStatement("delete from ja_snitches where id = ?;")) {
			deleteSnitch.setInt(1, snitch.getId());
			deleteSnitch.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to delete snitch: ", e);
		}
	}

	@Override
	public void loadAll(Consumer<Snitch> insertFunction) {
		SnitchTypeManager configMan = JukeAlert.getInstance().getSnitchConfigManager();
		SnitchManager snitchMan = JukeAlert.getInstance().getSnitchManager();
		WorldIDManager idMan = CivModCorePlugin.getInstance().getWorldIdManager();
		try (Connection insertConn = db.getConnection();
				PreparedStatement selectSnitch = insertConn
						.prepareStatement("select x, y, z, type_id, group_id, name, id, world_id from ja_snitches");
				ResultSet rs = selectSnitch.executeQuery()) {
			while (rs.next()) {
				int x = rs.getInt(1);
				int y = rs.getInt(2);
				int z = rs.getInt(3);
				short worldId = rs.getShort(8);
				World world = idMan.getWorldByInternalID(worldId);
				Location location = new Location(world, x, y, z);
				int typeID = rs.getInt(4);
				SnitchFactoryType type = configMan.getConfig(typeID);
				if (type == null) {
					logger.log(Level.SEVERE, "Failed to load snitch with type id " + typeID);
					continue;
				}
				int groupID = rs.getInt(5);
				if (groupID == -1) {
					continue;
				}
				String name = rs.getString(6);
				int id = rs.getInt(7);
				Snitch snitch = type.create(id, location, name, groupID, false);
				insertFunction.accept(snitch);
				snitchMan.addSnitchToQuadTree(snitch);
				snitch.applyToAppenders(AbstractSnitchAppender::postSetup);
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
						"select jsa.name, jse.uuid, jse.x, jse.y, jse.z, jse.creation_time, jse.victim, jse.id"
								+ " from ja_snitch_entries jse inner join ja_snitch_actions jsa on "
								+ "jse.type_id = jsa.id where snitch_id = ? order by jse.creation_time asc;");) {
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
					int logId = rs.getInt(8);
					Location loc = new Location(snitch.getLocation().getWorld(), x, y, z);
					LoggableAction action = factory.produce(snitch, identifier, uuid, loc, time, victim);
					if (action != null) {
						action.setID(logId);
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

	public void insertLogAsync(int typeID, Snitch snitch, LoggablePlayerAction action) {
		Bukkit.getScheduler().runTaskAsynchronously(JukeAlert.getInstance(), () -> {
			insertLog(typeID, snitch, action.getPersistence());
			action.setCacheState(ActionCacheState.NORMAL);
		});
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
				logger.log(Level.SEVERE, "Found no refresh timer for snitch with id " + snitchID);
				return -1;
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to retrieve refresh timer", e);
			return -1;
		}
	}

	public List<Snitch> loadSnitchesByGroupID(Collection<Integer> groupIDs) {
		SnitchTypeManager configMan = JukeAlert.getInstance().getSnitchConfigManager();
		WorldIDManager gcmm = CivModCorePlugin.getInstance().getWorldIdManager();
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
							SnitchFactoryType type = configMan.getConfig(typeID);
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
