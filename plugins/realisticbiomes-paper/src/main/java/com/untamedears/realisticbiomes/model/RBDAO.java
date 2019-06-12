package com.untamedears.realisticbiomes.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;

import com.untamedears.realisticbiomes.RealisticBiomes;

import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class RBDAO {

	private ManagedDatasource db;
	private Logger logger;

	public RBDAO(ManagedDatasource db, RealisticBiomes plugin) {
		this.db = db;
		this.logger = plugin.getLogger();
	}

	private void deletePlant(ChunkCache cache, Plant plant, PreparedStatement deleteStatement) throws SQLException {
		deleteStatement.setInt(1, plant.getLocation().getBlockX());
		deleteStatement.setInt(2, plant.getLocation().getBlockY());
		deleteStatement.setInt(3, plant.getLocation().getBlockZ());
		deleteStatement.setInt(4, cache.getWorldID());
		deleteStatement.addBatch();
	}

	public int getOrCreateWorldID(World world) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement insertWorld = insertConn
						.prepareStatement("select id from rb_plant_worlds where uuid = ?;")) {
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
						"insert into rb_plant_worlds (uuid, name) values(?,?);", Statement.RETURN_GENERATED_KEYS);) {
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

	private void insertNewPlant(ChunkCache cache, Plant plant, PreparedStatement insertStatement) throws SQLException {
		insertStatement.setInt(1, plant.getLocation().getBlockX());
		insertStatement.setInt(2, plant.getLocation().getBlockY());
		insertStatement.setInt(3, plant.getLocation().getBlockZ());
		insertStatement.setInt(4, cache.getChunkPair().getX());
		insertStatement.setInt(5, cache.getChunkPair().getZ());
		insertStatement.setInt(6, cache.getWorldID());
		insertStatement.setTimestamp(7, new Timestamp(plant.getCreationTime()));
		insertStatement.addBatch();
	}

	public ChunkCache loadPlants(ChunkCoord coords, WorldPlantManager worldManager) {
		List<Plant> plantList = new ArrayList<>();
		try (Connection loadConn = db.getConnection();
				PreparedStatement loadRein = loadConn
						.prepareStatement("select x, y, z, creation_time, id from rb_plants "
								+ "where chunk_x = ? and chunk_z = ? and world_id = ?;")) {
			loadRein.setInt(1, coords.getX());
			loadRein.setInt(2, coords.getZ());
			loadRein.setInt(3, worldManager.getWorldID());
			try (ResultSet rs = loadRein.executeQuery()) {
				while (rs.next()) {
					int x = rs.getInt(1);
					int y = rs.getInt(2);
					int z = rs.getInt(3);
					long millisCreation = rs.getTimestamp(4).getTime();
					Location loc = new Location(worldManager.getWorld(), x, y, z);
					plantList.add(new Plant(millisCreation, loc, false, false));
				}
			}
		} catch (SQLException e) {
			logger.severe("Failed to load plants: " + e.toString());
		}
		return new ChunkCache(worldManager, coords, plantList);
	}

	public void savePlants(ChunkCache cache) {
		if (!cache.isDirty()) {
			return;
		}
		try (Connection conn = db.getConnection();
				PreparedStatement insertStatement = conn.prepareStatement(
						"insert into rb_plants (x,y,z,chunk_x,chunk_z,world_id,creation_time) values(?,?,?,?,?,?,?);");
				PreparedStatement deleteStatement = conn
						.prepareStatement("delete from rb_plants where x = ? and y = ? and z = ? and world_id = ?;")) {
			for (Plant plant : cache.getAllAndCleanUp()) {
				if (!plant.isDirty()) {
					continue;
				}
				if (plant.isNew()) {
					if (!plant.isDeleted()) {
						insertNewPlant(cache, plant, insertStatement);
					}
				} else {
					deletePlant(cache, plant, deleteStatement);

				}
				plant.setDirty(false);
			}
			// deletes before inserts in case a plant was destroyed and then
			// recreated
			deleteStatement.executeBatch();
			insertStatement.executeBatch();
			cache.setDirty(false);
		} catch (SQLException e) {
			logger.severe("Failed to update plant data: " + e.toString());
		}
	}

	public boolean update() {
		db.registerMigration(1, false, new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				// TODO Auto-generated method stub
				return true;
			}
		}, "create table rb_plant_worlds (id int not null auto_increment primary key, uuid char(36) not null, "
				+ "name text not null, constraint uniqueUuid unique(uuid));",
				"create table rb_plants (id int not null auto_increment primary key, x int not null, y int not null, z int not null, "
						+ "chunk_x int not null, chunk_z int not null, world_id int not null references rb_plant_worlds(id) "
						+ ", creation_time timestamp not null default now(), index reinChunkLookUp(chunk_x, chunk_z, world_id),"
						+ "index reinCoordLookUp (x, y, z, world_id), constraint reinforcementUniqueLocation unique (x,y,z,world_id));");
		return db.updateDatabase();
	}

}
