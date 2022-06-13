package com.untamedears.realisticbiomes.model;

import com.untamedears.realisticbiomes.GrowthConfigManager;
import com.untamedears.realisticbiomes.PlantLogicManager;
import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.CacheState;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.XZWCoord;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.BlockBasedChunkMeta;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableBasedBlockChunkMeta;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableStorageEngine;

public class RBDAO extends TableStorageEngine<Plant> {

	private boolean batchMode;
	private List<List<PlantTuple>> batches;

	public RBDAO(Logger logger, ManagedDatasource db) {
		super(logger, db);
		this.batchMode = false;
	}

	public void setBatchMode(boolean batch) {
		this.batchMode = batch;
		batches = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			batches.add(new ArrayList<>());
		}
	}

	public void cleanupBatches() {
		long currentTime = System.currentTimeMillis();
		try (Connection conn = db.getConnection();
				PreparedStatement deletePlant = conn.prepareStatement(
						"delete from rb_plants where chunk_x = ? and chunk_z = ? and world_id = ? and "
								+ "x_offset = ? and y = ? and z_offset = ?;");) {
			conn.setAutoCommit(false);
			for (PlantTuple tuple : batches.get(2)) {
				setDeleteDataStatement(deletePlant, tuple.plant, tuple.coord);
				deletePlant.addBatch();
			}
			logger.info("Batch 2: " + (System.currentTimeMillis() - currentTime) + " ms");
			logger.info("Batch 2 Size: " + batches.get(2).size());
			batches.get(2).clear();
			deletePlant.executeBatch();
			conn.setAutoCommit(true);
			logger.info("Batch 2 Finish: " + (System.currentTimeMillis() - currentTime) + " ms");
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to delete plant from db: ", e);
		}
		try (Connection conn = db.getConnection();
				PreparedStatement insertPlant = conn.prepareStatement(
						"insert ignore into rb_plants (chunk_x, chunk_z, world_id, x_offset, y, z_offset, creation_time, type) "
								+ "values(?,?,?, ?,?,?, ?,?);");) {
			conn.setAutoCommit(false);
			for (PlantTuple tuple : batches.get(0)) {
				setInsertDataStatement(insertPlant, tuple.plant, tuple.coord);
				insertPlant.addBatch();
			}
			logger.info("Batch 0: " + (System.currentTimeMillis() - currentTime) + " ms");
			logger.info("Batch 0 Size: " + batches.get(0).size());
			batches.get(0).clear();
			insertPlant.executeBatch();
			conn.setAutoCommit(true);
			logger.info("Batch 0 Finish: " + (System.currentTimeMillis() - currentTime) + " ms");
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to insert plant into db: ", e);
		}
		try (Connection conn = db.getConnection();
				PreparedStatement updatePlant = conn.prepareStatement("update rb_plants set creation_time = ?, type = ? where "
						+ "chunk_x = ? and chunk_z = ? and world_id = ? and x_offset = ? and y = ? and z_offset = ?;");) {
			conn.setAutoCommit(false);
			for (PlantTuple tuple : batches.get(1)) {
				setUpdateDataStatement(updatePlant, tuple.plant, tuple.coord);
				updatePlant.addBatch();
			}
			logger.info("Batch 1: " + (System.currentTimeMillis() - currentTime) + " ms");
			logger.info("Batch 1 Size: " + batches.get(1).size());
			batches.get(1).clear();
			updatePlant.executeBatch();
			conn.setAutoCommit(true);
			logger.info("Batch 1 Finish: " + (System.currentTimeMillis() - currentTime) + " ms");
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to update plant in db: ", e);
		}
	}

	@Override
	public void delete(Plant data, XZWCoord coord) {
		if (batchMode) {
			batches.get(2).add(new PlantTuple(data, coord));
			return;
		}
		try (Connection conn = db.getConnection();
				PreparedStatement deletePlant = conn.prepareStatement(
						"delete from rb_plants where chunk_x = ? and chunk_z = ? and world_id = ? and "
								+ "x_offset = ? and y = ? and z_offset = ?;");) {
			setDeleteDataStatement(deletePlant, data, coord);
			deletePlant.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to delete plant from db: ", e);
		}
	}
	
	private static void setDeleteDataStatement(PreparedStatement deletePlant, Plant data, XZWCoord coord) throws SQLException {
		deletePlant.setInt(1, coord.getX());
		deletePlant.setInt(2, coord.getZ());
		deletePlant.setShort(3, coord.getWorldID());
		deletePlant.setByte(4, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockX()));
		deletePlant.setShort(5, (short) data.getLocation().getBlockY());
		deletePlant.setByte(6, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ()));
	}

	@Override
	public void fill(TableBasedBlockChunkMeta<Plant> chunkData, Consumer<Plant> insertFunction) {
		int preMultipliedX = chunkData.getChunkCoord().getX() * 16;
		int preMultipliedZ = chunkData.getChunkCoord().getZ() * 16;
		List<Plant> toUpdate = new ArrayList<>();
		PlantLogicManager logicMan = RealisticBiomes.getInstance().getPlantLogicManager();
		GrowthConfigManager growthConfigMan = RealisticBiomes.getInstance().getGrowthConfigManager();
		World world = chunkData.getChunkCoord().getWorld();
		try (Connection insertConn = db.getConnection();
				PreparedStatement selectPlant = insertConn
						.prepareStatement("select x_offset, y, z_offset, creation_time, type "
								+ "from rb_plants where chunk_x = ? and chunk_z = ? and world_id = ?;");) {
			selectPlant.setInt(1, chunkData.getChunkCoord().getX());
			selectPlant.setInt(2, chunkData.getChunkCoord().getZ());
			selectPlant.setShort(3, chunkData.getChunkCoord().getWorldID());
			try (ResultSet rs = selectPlant.executeQuery()) {
				while (rs.next()) {
					int xOffset = rs.getByte(1);
					int x = xOffset + preMultipliedX;
					int y = rs.getShort(2);
					int zOffset = rs.getByte(3);
					int z = zOffset + preMultipliedZ;
					Location location = new Location(world, x, y, z);
					long creationTime = rs.getTimestamp(4).getTime();
					short configId = rs.getShort(5);
					PlantGrowthConfig growthConfig = null;
					if (configId != 0) {
						growthConfig = growthConfigMan.getConfigById(configId);
					}
					Plant plant = new Plant(creationTime, location, false, growthConfig);
					toUpdate.add(plant);
					insertFunction.accept(plant);
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to load plant from db: ", e);
		}
		Bukkit.getScheduler().runTask(RealisticBiomes.getInstance(), () -> {
			for (Plant plant : toUpdate) {
				if (plant.getCacheState() == CacheState.DELETED) {
					continue;
				}
				logicMan.updateGrowthTime(plant, plant.getLocation().getBlock());
			}
		});
	}

	@Override
	public void insert(Plant data, XZWCoord coord) {
		if (batchMode) {
			batches.get(0).add(new PlantTuple(data, coord));
			return;
		}
		try (Connection conn = db.getConnection();
				PreparedStatement insertPlant = conn.prepareStatement(
						"insert into rb_plants (chunk_x, chunk_z, world_id, x_offset, y, z_offset, creation_time, type) "
								+ "values(?,?,?, ?,?,?, ?,?);");) {
			setInsertDataStatement(insertPlant,data, coord);
			insertPlant.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to insert plant into db: ", e);
		}
	}
	
	private static void setInsertDataStatement(PreparedStatement insertPlant, Plant data, XZWCoord coord) throws SQLException {
		insertPlant.setInt(1, coord.getX());
		insertPlant.setInt(2, coord.getZ());
		insertPlant.setShort(3, coord.getWorldID());
		insertPlant.setByte(4, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockX()));
		insertPlant.setShort(5, (short) data.getLocation().getBlockY());
		insertPlant.setByte(6, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ()));
		insertPlant.setTimestamp(7, new Timestamp(data.getCreationTime()));
		if (data.getGrowthConfig() == null) {
			insertPlant.setNull(8, Types.SMALLINT);
		}
		else {
			insertPlant.setShort(8, data.getGrowthConfig().getID());
		}
	}

	@Override
	public void registerMigrations() {
		db.registerMigration(1, false,
				"CREATE TABLE IF NOT EXISTS rb_plant (chunkId bigint(20) DEFAULT NULL, w int(11) DEFAULT NULL,"
						+ "x int(11) DEFAULT NULL, y int(11) DEFAULT NULL, z int(11) DEFAULT NULL, date int(10) unsigned DEFAULT NULL,"
						+ "growth double DEFAULT NULL, fruitGrowth double DEFAULT NULL)",
				"CREATE TABLE IF NOT EXISTS rb_chunk (id bigint(20) NOT NULL AUTO_INCREMENT, w int(11) DEFAULT NULL, x int(11) DEFAULT NULL, "
						+ "z int(11) DEFAULT NULL, PRIMARY KEY (id), KEY chunk_coords_idx (w,x,z))");
		db.registerMigration(2, false,
				"create table if not exists rb_plants (chunk_x int not null, chunk_z int not null, world_id smallint unsigned not null, "
						+ "x_offset tinyint unsigned not null, y tinyint unsigned not null, z_offset tinyint unsigned not null,"
						+ "creation_time timestamp not null default now(), index plantChunkLookUp(chunk_x, chunk_z, world_id),"
						+ "index plantCoordLookUp (x_offset, y, z_offset, world_id), "
						+ "constraint plantUniqueLocation unique (chunk_x,chunk_z,x_offset,y,z_offset,world_id));",
				"delete from rb_plants",
				"insert into rb_plants (chunk_x, chunk_z, world_id, x_offset, y, z_offset, creation_time) "
						+ "select c.x,c.z,c.w + 1, if(mod(p.x,16)<0,mod(p.x,16)+16,mod(p.x,16)), p.y, "
						+ "if(mod(p.z,16)<0,mod(p.z,16)+16,mod(p.z,16)),FROM_UNIXTIME(p.date) "
						+ "from rb_plant p inner join rb_chunk c on p.chunkId=c.id;");
		db.registerMigration(3, false,
				"alter table rb_plants add type smallint");
		db.registerMigration(4, false,
				"alter table rb_plants modify y tinyint signed not null");
		db.registerMigration(5, false,
				"alter table rb_plants modify y smallint not null");
	}

	@Override
	public void update(Plant data, XZWCoord coord) {
		if (batchMode) {
			batches.get(1).add(new PlantTuple(data, coord));
			return;
		}
		try (Connection conn = db.getConnection();
				PreparedStatement updatePlant = conn.prepareStatement("update rb_plants set creation_time = ?, type = ? where "
						+ "chunk_x = ? and chunk_z = ? and world_id = ? and x_offset = ? and y = ? and z_offset = ?;");) {
			setUpdateDataStatement(updatePlant, data, coord);
			updatePlant.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to update plant in db: ", e);
		}
	}
	
	private static void setUpdateDataStatement(PreparedStatement updatePlant, Plant data, XZWCoord coord) throws SQLException {
		updatePlant.setTimestamp(1, new Timestamp(data.getCreationTime()));
		if (data.getGrowthConfig() == null) {
			updatePlant.setNull(2, Types.SMALLINT);
		}
		else {
			updatePlant.setShort(2, data.getGrowthConfig().getID());
		}
		updatePlant.setInt(3, coord.getX());
		updatePlant.setInt(4, coord.getZ());
		updatePlant.setShort(5, coord.getWorldID());
		updatePlant.setByte(6, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockX()));
		updatePlant.setShort(7, (short) data.getLocation().getBlockY());
		updatePlant.setByte(8, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ()));
	}

	@Override
	public TableBasedDataObject getForLocation(int x, int y, int z, short worldID, short pluginID) {
		throw new IllegalStateException("Can not load plant when chunk is not loaded");
	}

	@Override
	public Collection<XZWCoord> getAllDataChunks() {
		List<XZWCoord> result = new ArrayList<>();
		try (Connection insertConn = db.getConnection();
				PreparedStatement selectChunks = insertConn.prepareStatement(
						"select chunk_x, chunk_z, world_id from rb_plants group by chunk_x, chunk_z, world_id");
				ResultSet rs = selectChunks.executeQuery()) {
			while (rs.next()) {
				int chunkX = rs.getInt(1);
				int chunkZ = rs.getInt(2);
				short worldID = rs.getShort(3);
				result.add(new XZWCoord(chunkX, chunkZ, worldID));
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to select populated chunks from db: ", e);
		}
		return result;
	}

	@Override
	public boolean stayLoaded() {
		return false;
	}

	private class PlantTuple {
		private Plant plant;
		private XZWCoord coord;

		PlantTuple(Plant plant, XZWCoord coord) {
			this.plant = plant;
			this.coord = coord;
		}
	}

}
