package vg.civcraft.mc.citadel.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.model.ChunkCache;
import vg.civcraft.mc.citadel.model.ChunkCoord;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementTypeManager;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class CitadelReinforcementData {

	private ManagedDatasource db;
	private Logger logger;
	private ReinforcementTypeManager typeMan;

	public CitadelReinforcementData(ManagedDatasource db, Citadel plugin, ReinforcementTypeManager typeMan) {
		this.db = db;
		this.typeMan = typeMan;
		this.logger = plugin.getLogger();
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

	public ChunkCache loadReinforcements(ChunkCoord coords, int worldID) {
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

	private void registerMigrations() {
		db.registerMigration(13, false, "create table reinforcement_worlds (id int not null auto_increment primary key, uuid char(36) not null, "
				+ "name text not null, constraint uniqueUuid unique(uuid));",
				"create table reinforcements (id int not null auto_increment primary key, x int not null, y int not null, z int not null, "
						+ "chunk_x int not null, chunk_z int not null, world_id int not null references reinforcement_worlds(id), "
						+ "type_id int not null, creation_time timestamp not null default now(), health double not null, "
						+ "group_id int not null, insecure boolean not null default false, index reinChunkLookUp(chunk_x, chunk_z, world_id),"
						+ "constraint reinforcementUniqueLocation unique (x,y,z,world_id));");
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
			// deletes before inserts in case a reinforcement was destroyed and then
			// recreated
			deleteStatement.executeBatch();
			insertStatement.executeBatch();
			updateStatement.executeBatch();
			cache.setDirty(false);
		} catch (SQLException e) {
			logger.severe("Failed to update reinforcement data: " + e.toString());
		}
	}

	public boolean startUp() {
		registerMigrations();
		return db.updateDatabase();
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

}
