package vg.civcraft.mc.citadel.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.citadel.ChunkCache;
import vg.civcraft.mc.citadel.ChunkPair;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementTypeManager;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;

public class CitadelReinforcementData {

	private ManagedDatasource db;
	private Logger logger;
	private ReinforcementTypeManager typeMan;

	public CitadelReinforcementData(ManagedDatasource db, Citadel plugin, ReinforcementTypeManager typeMan) {
		this.db = db;
		this.typeMan = typeMan;
		this.logger = plugin.getLogger();
	}

	public boolean startUp() {
		registerMigrations();
		return db.updateDatabase();
	}

	private void registerMigrations() {
		db.registerMigration(7, false,
				"create table if not exists reinforcement_id(" + "rein_id int not null auto_increment,"
						+ "x int not null," + "y int not null," + "z int not null," + "chunk_id varchar(255),"
						+ "world varchar (255) not null," + "primary key rein_id_key (rein_id),"
						+ "unique key x_y_z_world(x,y,z,world));-- Your mother is a whore and sleeps with banjos",
				// I like turtles mother fucker. Never program because then you get turtles.
				"insert into reinforcement_id (x, y, z, chunk_id, world) select x, y, z, chunk_id, world from reinforcement;", // populate
																																// that
																																// bitch.
				"alter table reinforcement add rein_id int not null, drop chunk_id;",
				"update reinforcement r inner join reinforcement_id ri on "
						+ "ri.x = r.x and ri.y = r.y and ri.z = r.z and ri.world = r.world "
						+ "set r.rein_id = ri.rein_id",
				"alter table reinforcement DROP PRIMARY KEY, " + "add primary key rein_id_key(rein_id), " + "drop x,"
						+ "drop y," + "drop z," + "drop world;",
				"alter table reinforcement_id add index `chunk_id_index` (chunk_id);");
		db.registerMigration(8, false,
				"alter table reinforcement_id drop primary key," + " add primary key (rein_id, x, y, z, world);");
		db.registerMigration(9, false, "alter table reinforcement add acid_time int not null;",
				"update reinforcement set acid_time = maturation_time;"); // Might take a minute.
		db.registerMigration(10, false, "drop procedure if exists insertReinID;",
				"create definer=current_user procedure insertReinID(" + "in x int," + "in y int," + "in z int,"
						+ "in chunk_id varchar(255)," + "in world varchar(255)" + ") sql security invoker begin "
						+ "insert into reinforcement_id(x, y, z, chunk_id, world) values (x, y, z, chunk_id, world);"
						+ "select LAST_INSERT_ID() as id;" + "end;",
				"drop procedure if exists insertCustomReinID;",
				"create definer=current_user procedure insertCustomReinID(" + "in rein_id int," + "in x int,"
						+ "in y int," + "in z int," + "in chunk_id varchar(255)," + "in world varchar(255)"
						+ ") sql security invoker begin "
						+ "insert into reinforcement_id(rein_id, x, y, z, chunk_id, world) values (rein_id, x, y, z, chunk_id, world);"
						+ "select LAST_INSERT_ID() as id;" + "end;");
		db.registerMigration(11, false, "drop procedure if exists insertRein;",
				"create definer=current_user procedure insertRein(" + "in x int," + "in y int," + "in z int,"
						+ "in chunk_id varchar(255)," + "in world varchar(255)," + "in material_id int,"
						+ "in durability varchar(10)," + "in insecure tinyint(1)," + "in group_id int,"
						+ "in maturation_time int," + "in lore varchar(255)," + "in acid_time int,"
						+ "in rein_type varchar(30)" + ") sql security invoker begin "
						+ "insert into reinforcement_id(x, y, z, chunk_id, world) values (x, y, z, chunk_id, world);"
						+ "insert into reinforcement ("
						+ "material_id, durability, insecure, group_id, maturation_time, rein_type_id, lore, rein_id, acid_time) VALUES ("
						+ "material_id, durability, insecure, group_id, maturation_time, "
						+ "(SELECT rt.rein_type_id FROM reinforcement_type rt where rt.rein_type = rein_type LIMIT 1), "
						+ "lore, (select LAST_INSERT_ID()), acid_time);" + "end;");
		db.registerMigration(12, false,
				"CREATE TABLE reinforcement_temp (" + "rein_id int not null auto_increment," + "x int not null,"
						+ "y int not null," + "z int not null," + "chunk_x int not null," + "chunk_z int not null,"
						+ "world varchar(255) not null," + "material_id int not null,"
						+ "durability varchar(10) not null," + "insecure tinyint(1) not null,"
						+ "group_id int not null," + "maturation_time int not null," + "rein_type_id int not null,"
						+ "lore varchar(255)," + "acid_time int not null," + "primary key rid (rein_id),"
						+ "unique index realcoord (x,y,z,world)," + "index chunkcoord(chunk_x, chunk_z, world)" + ");",
				"INSERT IGNORE INTO reinforcement_temp SELECT a.rein_id, x, y, z, floor(x/16), floor(z/16), world, "
						+ "material_id, durability, insecure, group_id, maturation_time, rein_type_id, lore, acid_time "
						+ "FROM reinforcement_id a JOIN reinforcement b ON a.rein_id = b.rein_id;",
				"RENAME TABLE reinforcement_id TO deprecated_reinforcement_id;",
				"RENAME TABLE reinforcement TO deprecated_reinforcement;",
				"RENAME TABLE reinforcement_temp TO reinforcement;", "DROP PROCEDURE IF EXISTS insertReinID;",
				"DROP PROCEDURE IF EXISTS insertCustomReinID;", "DROP PROCEDURE IF EXISTS insertRein;");
		db.registerMigration(13, false, new Callable<Boolean>() {

			@Override
			public Boolean call() throws SQLException {
				logger.info("Upgrading to Citadel 4.0. This may take a while.");
				int failedUpdates = 0;
				int successfulUpdates = 0;
				Map<String, Integer> worldMapping = new HashMap<>();
				for (World world : Bukkit.getWorlds()) {
					int id = getOrCreateWorldID(world);
					if (id == -1) {
						return false;
					}
					worldMapping.put(world.getName(), id);

				}
				try (Connection connection = db.getConnection();
						PreparedStatement getRein = connection.prepareStatement(
								"select rein_id, x, y, z, chunk_x, chunk_z, world, material_id, durability, insecure, group_id, "
										+ "lore, acid_time from reinforcement;");
						ResultSet rs = getRein.executeQuery()) {
					while (rs.next()) {
						int id = rs.getInt(1);
						int x = rs.getInt(2);
						int y = rs.getInt(3);
						int z = rs.getInt(4);
						int chunk_x = rs.getInt(5);
						int chunk_z = rs.getInt(6);
						String worldName = rs.getString(7);
						int materialID = rs.getInt(8);
						String durability = rs.getString(9);
						double health = Double.parseDouble(durability);
						boolean insecure = rs.getBoolean(10);
						int groupId = rs.getInt(11);
						String lore = rs.getString(12);
						int acidTime = rs.getInt(13);
						long msAcidTime = acidTime;
						msAcidTime *= 60000;
						if (msAcidTime == 0) {
							msAcidTime = System.currentTimeMillis();
						}
						@SuppressWarnings("deprecation")
						Material mat = Material.getMaterial(materialID);
						if (mat == null) {
							failedUpdates++;
							continue;
						}
						ItemStack is = new ItemStack(mat);
						if (lore != null) {
							ISUtils.addLore(is, lore);
						}
						ReinforcementType type = typeMan.getByItemStack(is);
						if (type == null) {
							failedUpdates++;
							continue;
						}
						int typeID = type.getID();
						Integer worldID = worldMapping.get(worldName);
						if (worldID == null) {
							failedUpdates++;
							continue;
						}
						try (Connection insertConn = db.getConnection();
								PreparedStatement insertRein = insertConn.prepareStatement(
										"insert into reinforcements (id,x,y,z,chunk_x,chunk_z,world_id,type_id,"
												+ "creation_time,health,group_id,insecure) values(?,?,?,?,?,?,?,?,?,?,?,?);");) {
							insertRein.setInt(1, id);
							insertRein.setInt(2, x);
							insertRein.setInt(3, y);
							insertRein.setInt(4, z);
							insertRein.setInt(5, chunk_x);
							insertRein.setInt(6, chunk_z);
							insertRein.setInt(7, worldID);
							insertRein.setInt(8, typeID);
							insertRein.setTime(9, new Time(msAcidTime));
							insertRein.setDouble(10, health);
							insertRein.setInt(11, groupId);
							insertRein.setBoolean(12, insecure);
							insertRein.execute();
						}
						try (Connection deleteConn = db.getConnection();
								PreparedStatement deleteRein = deleteConn
										.prepareStatement("delete from reinforcement where id = ?;");) {
							deleteRein.setInt(1, id);
							deleteRein.execute();
						}
						successfulUpdates++;
					}
				}
				logger.info("Completed Citadel 4.0 update. Successfull: " + successfulUpdates + ", Failed: "
						+ failedUpdates);
				if (failedUpdates > 0) {
					logger.severe("Some of your old data could not be transfered, it was left intact in the old table. "
							+ "Contact Citadel developers if you do not know how to fix this yourself");
				}
				return true;
			}
		}, "create table reinforcement_worlds (id int not null autoincrement, uuid char(36) not null unique, name text not null);",
				"create table reinforcements (id int not null auto_increment, x int not null, y int not null, z int not null, "
						+ "chunk_x int not null, chunk_z int not null, world_id int not null references reinforcement_worlds(id), "
						+ "type_id int not null, creation_time timestamp not null default now(), health double not null, "
						+ "group_id int not null, insecure boolean not null default false, index reinChunkLookUp(chunk_x, chunk_z, world_id),"
						+ "constraint reinforcementUniqueLocation unique (x,y,z,world_id));");
	}

	public ChunkCache loadReinforcements(ChunkPair coords, int worldID) {
		List<Reinforcement> reinforcements = new ArrayList<>();
		World world = null;
		try (Connection loadConn = db.getConnection();
				PreparedStatement loadRein = loadConn.prepareStatement(
						"select x, y, z, type_id, creation_time, health, group_id, insecure from reinforcements "
								+ "where chunk_x = ? and chunk_z = ? and world_id = ?;")) {
			loadRein.setInt(1, coords.getX());
			loadRein.setInt(2, coords.getZ());
			loadRein.setInt(3, worldID);
			try (ResultSet rs = loadRein.executeQuery()) {
				while (rs.next()) {
					int x = rs.getInt(1);
					int y = rs.getInt(2);
					int z = rs.getInt(3);
					int typeId = rs.getInt(4);
					long millisCreation = rs.getTimestamp(5).getTime();
					double health = rs.getDouble(6);
					int groupId = rs.getInt(7);
					boolean insecure = rs.getBoolean(8);
					ReinforcementType type = typeMan.getById(typeId);
					Location loc = new Location(world, x, y, z);
					if (type == null) {
						logger.warning("Ignoring reinforcement at " + loc.toString() + " because of invalid type id "
								+ typeId);
						continue;
					}
					reinforcements
							.add(new Reinforcement(loc, type, groupId, millisCreation, health, false, false, insecure));
				}
			}
		} catch (SQLException e) {
			logger.severe("Failed to load reinforcements: " + e.toString());
		}
		return new ChunkCache(coords, reinforcements, worldID);
	}

	public void saveReinforcements(ChunkCache cache) {
		if (!cache.isDirty()) {
			return;
		}
		try (Connection conn = db.getConnection()) {
			PreparedStatement insertStatement = conn
					.prepareStatement("insert into reinforcements (x,y,z,chunk_x,chunk_z,world_id,type_id,"
							+ "health,group_id,insecure) values(?,?,?,?,?,?,?,?,?,?);");
			PreparedStatement deleteStatement = conn
					.prepareStatement("delete from reinforcements where x = ? and y = ? and z = ? and world_id = ?;");
			PreparedStatement updateStatement = conn.prepareStatement("update reinforcements "
					+ "set insecure = ?, health=?, type_id=? group_id=? where x = ? and y = ? and z = ? and world_id = ?;");
			for (Reinforcement rein : cache.getAllAndCleanUp()) {
				if (!rein.isDirty()) {
					continue;
				}
				if (rein.isNew()) {
					if (!rein.isBroken()) {
						insertNewReinforcement(cache, rein, insertStatement);
					}
				} else {
					if (rein.isBroken()) {
						deleteReinforcement(cache, rein, deleteStatement);
					} else {
						updateReinforcement(cache, rein, updateStatement);
					}
				}
				rein.setDirty(false);
			}
			//deletes before inserts in case a reinforcement was destroyed and then recreated
			deleteStatement.executeBatch();
			insertStatement.executeBatch();
			updateStatement.executeBatch();
			cache.setDirty(false);
		} catch (SQLException e) {
			logger.severe("Failed to update reinforcement data: " + e.toString());
		}
	}

	private void insertNewReinforcement(ChunkCache cache, Reinforcement rein, PreparedStatement insertStatement)
			throws SQLException {
		insertStatement.setInt(1, rein.getLocation().getBlockX());
		insertStatement.setInt(2, rein.getLocation().getBlockY());
		insertStatement.setInt(3, rein.getLocation().getBlockZ());
		insertStatement.setInt(4, cache.getChunkPair().getX());
		insertStatement.setInt(5, cache.getChunkPair().getZ());
		insertStatement.setInt(6, cache.getWorldID());
		insertStatement.setInt(7, rein.getType().getID());
		insertStatement.setDouble(8, rein.getHealth());
		insertStatement.setInt(9, rein.getGroupId());
		insertStatement.setBoolean(10, rein.isInsecure());
		insertStatement.addBatch();
	}

	private void updateReinforcement(ChunkCache cache, Reinforcement rein, PreparedStatement updateStatement)
			throws SQLException {
		updateStatement.setBoolean(1, rein.isInsecure());
		updateStatement.setDouble(2, rein.getHealth());
		updateStatement.setInt(3, rein.getType().getID());
		updateStatement.setInt(4, rein.getGroupId());
		updateStatement.setInt(5, rein.getLocation().getBlockX());
		updateStatement.setInt(6, rein.getLocation().getBlockY());
		updateStatement.setInt(7, rein.getLocation().getBlockZ());
		updateStatement.setInt(8, cache.getWorldID());
		updateStatement.addBatch();
	}

	private void deleteReinforcement(ChunkCache cache, Reinforcement rein, PreparedStatement deleteStatement)
			throws SQLException {
		deleteStatement.setInt(1, rein.getLocation().getBlockX());
		deleteStatement.setInt(2, rein.getLocation().getBlockY());
		deleteStatement.setInt(3, rein.getLocation().getBlockZ());
		deleteStatement.setInt(4, cache.getWorldID());
		deleteStatement.addBatch();
	}

	public int getOrCreateWorldID(World world) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement insertWorld = insertConn
						.prepareStatement("select id from reinforcement_worlds where uuid = ?;")) {
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
						"insert into reinforcement_worlds (uuid, name) values(?,?);",
						Statement.RETURN_GENERATED_KEYS);) {
			insertWorld.setString(1, world.getUID().toString());
			insertWorld.setString(2, world.getName());
			try (ResultSet rs = insertWorld.executeQuery()) {
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

}
