package com.untamedears.realisticbiomes.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.meta.ItemMeta;

import com.untamedears.realisticbiomes.PlantLogicManager;
import com.untamedears.realisticbiomes.RealisticBiomes;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.ChunkCoord;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.GlobalChunkMetaManager;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockBasedChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedBlockChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableStorageEngine;

public class RBDAO extends TableStorageEngine<Plant> {

	public RBDAO(Logger logger, ManagedDatasource db) {
		super(logger, db);
	}

	@Override
	public void delete(Plant data, ChunkCoord coord) {
		try (Connection conn = db.getConnection();
				PreparedStatement deletePlant = conn.prepareStatement(
						"delete from rb_plants where chunk_x = ? and chunk_z = ? and world_id = ? and "
								+ "x_offset = ? and y = ? and z_offset = ?;");) {
			deletePlant.setInt(1, coord.getX());
			deletePlant.setInt(2, coord.getZ());
			deletePlant.setShort(3, coord.getWorldID());
			deletePlant.setByte(4, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockX(), 16));
			deletePlant.setByte(5, (byte) data.getLocation().getBlockY());
			deletePlant.setByte(6, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ(), 16));
			deletePlant.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to delete plant from db: ", e);
		}
	}

	@Override
	public void fill(TableBasedBlockChunkMeta<Plant> chunkData, Consumer<Plant> insertFunction) {
		int preMultipliedX = chunkData.getChunkCoord().getX() * 16;
		int preMultipliedZ = chunkData.getChunkCoord().getZ() * 16;
		List<Plant> toUpdate = new ArrayList<>();
		PlantLogicManager logicMan = RealisticBiomes.getInstance().getPlantLogicManager();
		World world = chunkData.getChunkCoord().getWorld();
		try (Connection insertConn = db.getConnection();
				PreparedStatement selectPlant = insertConn
						.prepareStatement("select x_offset, y, z_offset, creation_time "
								+ "from rb_plants where chunk_x = ? and chunk_z = ? and world_id = ?;");) {
			selectPlant.setInt(1, chunkData.getChunkCoord().getX());
			selectPlant.setInt(2, chunkData.getChunkCoord().getZ());
			selectPlant.setShort(3, chunkData.getChunkCoord().getWorldID());
			try (ResultSet rs = selectPlant.executeQuery()) {
				while (rs.next()) {
					int xOffset = rs.getByte(1);
					int x = xOffset + preMultipliedX;
					int y = rs.getByte(2) & 0xFF;
					int zOffset = rs.getByte(3);
					int z = zOffset + preMultipliedZ;
					Location location = new Location(world, x, y, z);
					long creationTime = rs.getTimestamp(4).getTime();
					Plant plant = new Plant(creationTime, location, false);
					toUpdate.add(plant);
					insertFunction.accept(plant);
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to load plant from db: ", e);
		}
		Bukkit.getScheduler().runTask(RealisticBiomes.getInstance(), () -> {
			for (Plant plant : toUpdate) {
				logicMan.initGrowthTime(plant);
			}
		});

	}

	@Override
	public void insert(Plant data, ChunkCoord coord) {
		try (Connection conn = db.getConnection();
				PreparedStatement insertPlant = conn.prepareStatement(
						"insert into rb_plants (chunk_x, chunk_z, world_id, x_offset, y, z_offset, creation_time) "
								+ "values(?,?,?, ?,?,?, ?);");) {
			insertPlant.setInt(1, coord.getX());
			insertPlant.setInt(2, coord.getZ());
			insertPlant.setShort(3, coord.getWorldID());
			insertPlant.setByte(4, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockX(), 16));
			insertPlant.setByte(5, (byte) data.getLocation().getBlockY());
			insertPlant.setByte(6, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ(), 16));
			insertPlant.setTimestamp(7, new Timestamp(data.getCreationTime()));
			insertPlant.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to insert plant into db: ", e);
		}
	}

	@Override
	public void registerMigrations() {
		db.registerMigration(2, false, new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				try (Connection insertConn = db.getConnection();
						PreparedStatement selectPlant = insertConn.prepareStatement("select x,y,z,w,date from rb_plant");
						ResultSet rs = selectPlant.executeQuery()) {
					PreparedStatement insertPlant = insertConn.prepareStatement(
							"insert into rb_plants (chunk_x, chunk_z, world_id, x_offset, y, z_offset, creation_time) "
									+ "values(?,?,?, ?,?,?, ?);");
					try (PreparedStatement deleteExisting = insertConn
							.prepareStatement("delete from rb_plants")) {
						// in case this migration failed before some of the data might already have
						// migrated, which we want to undo
						deleteExisting.execute();
					}

					GlobalChunkMetaManager worldMan = CivModCorePlugin.getInstance().getChunkMetaManager();
					int batchCounter = 0;
					while (rs.next()) {
						int xCoord = rs.getInt(1);
						int yCoord = rs.getInt(2);
						int zCoord = rs.getInt(3);
						int worldId = rs.getInt(4);
						long timeStamp = rs.getInt(5);
						
						byte x = (byte) ((xCoord % 16) + 16);
						byte y = (byte) yCoord;
						byte z = (byte) ((yCoord % 16) + 16);
						int chunkX = xCoord / 16;
						int chunkZ = zCoord / 16;
						World world = Bukkit.getWorlds().get(worldId);
						short worldID = worldMan.getInternalWorldIdByName(world.getName());

						insertPlant.setInt(1, chunkX);
						insertPlant.setInt(2, chunkZ);
						insertPlant.setShort(3, worldID);
						insertPlant.setByte(4, x);
						insertPlant.setByte(5, y);
						insertPlant.setByte(6, z);
						insertPlant.setTimestamp(7, new Timestamp(timeStamp));
						insertPlant.addBatch();
						if (batchCounter > 100) {
							batchCounter = 0;
							insertPlant.executeBatch();
						}
						batchCounter++;
					}
					insertPlant.executeBatch();
				}
				return true;
			}
		}, "create table rb_plants (chunk_x int not null, chunk_z int not null, world_id smallint unsigned not null, "
				+ "x_offset tinyint unsigned not null, y tinyint unsigned not null, z_offset tinyint unsigned not null,"
				+ "creation_time timestamp not null default now(), index plantChunkLookUp(chunk_x, chunk_z, world_id),"
				+ "index plantCoordLookUp (x_offset, y, z_offset, world_id), "
				+ "constraint plantUniqueLocation unique (x_offset,y,z_offset,world_id));");
	}

	@Override
	public void update(Plant data, ChunkCoord coord) {
		try (Connection conn = db.getConnection();
				PreparedStatement updatePlant = conn.prepareStatement("update rb_plants set creation_time = ? where "
						+ "chunk_x = ? and chunk_z = ? and world_id = ? and x_offset = ? and y = ? and z_offset = ?;");) {
			updatePlant.setTimestamp(1, new Timestamp(data.getCreationTime()));
			updatePlant.setInt(2, coord.getX());
			updatePlant.setInt(3, coord.getZ());
			updatePlant.setShort(4, coord.getWorldID());
			updatePlant.setByte(5, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockX(), 16));
			updatePlant.setByte(6, (byte) data.getLocation().getBlockY());
			updatePlant.setByte(7, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ(), 16));
			updatePlant.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to update plant in db: ", e);
		}
	}

}
