package com.untamedears.realisticbiomes.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;

import com.untamedears.realisticbiomes.PlantLogicManager;
import com.untamedears.realisticbiomes.RealisticBiomes;

import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.ChunkCoord;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockBasedChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedBlockChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableStorageEngine;

public class RBDAO extends TableStorageEngine<Plant> {

	public RBDAO(Logger logger, ManagedDatasource db) {
		super(logger, db);
	}

	@Override
	public void registerMigrations() {
		//TODO migrate old data
		db.registerMigration(2, false, "create table rb_plants (chunk_x int not null, chunk_z int not null, world_id smallint unsigned not null, " + 
										"x_offset tinyint unsigned not null, y tinyint unsigned not null, z_offset tinyint unsigned not null,"
						+ ", creation_time timestamp not null default now(), index plantChunkLookUp(chunk_x, chunk_z, world_id),"
						+ "index plantCoordLookUp (x, y, z, world_id), constraint plantUniqueLocation unique (x,y,z,world_id));");
	}

	@Override
	public void insert(Plant data, ChunkCoord coord) {
		try (Connection conn = db.getConnection();
				PreparedStatement insertPlant = conn.prepareStatement(
						"insert into rb_plants (chunk_x, chunk_z, world_id, x_offset, y, z_offset, creation_time) "
						+ "values(?,?,?, ?,?,?, ?);");) {
			insertPlant.setInt(1, coord.getX());
			insertPlant.setInt(2, coord.getZ());
			insertPlant.setShort(3, (short) coord.getWorldID());
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
	public void update(Plant data, ChunkCoord coord) {
		try (Connection conn = db.getConnection();
				PreparedStatement updatePlant = conn.prepareStatement(
						"update rb_plants set creation_time = ? where "
								+ "chunk_x = ? and chunk_z = ? and world_id = ? and x_offset = ? and y = ? and z_offset = ?;");) {
			updatePlant.setTimestamp(1, new Timestamp(data.getCreationTime()));
			updatePlant.setInt(2, coord.getX());
			updatePlant.setInt(3, coord.getZ());
			updatePlant.setShort(4, (short) coord.getWorldID());
			updatePlant.setByte(5, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockX(), 16));
			updatePlant.setByte(6, (byte) data.getLocation().getBlockY());
			updatePlant.setByte(7,(byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ(), 16));
			updatePlant.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to update plant in db: ", e);
		}
	}

	@Override
	public void delete(Plant data, ChunkCoord coord) {
		try (Connection conn = db.getConnection();
				PreparedStatement deletePlant = conn.prepareStatement(
						"delete from rb_plants where chunk_x = ? and chunk_z = ? and world_id = ? and "
						+ "x_offset = ? and y = ? and z_offset = ?;");) {
			deletePlant.setInt(1, coord.getX());
			deletePlant.setInt(2, coord.getZ());
			deletePlant.setShort(3, (short) coord.getWorldID());
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
		PlantLogicManager logicMan = RealisticBiomes.getInstance().getPlantLogicManager();
		World world = chunkData.getChunkCoord().getWorld();
		try (Connection insertConn = db.getConnection();
				PreparedStatement selectPlant = insertConn.prepareStatement(
						"select x_offset, y, z_offset, creation_time "
								+ "from rb_plants where chunk_x = ? and chunk_z = ? and world_id = ?;");) {
			selectPlant.setInt(1, chunkData.getChunkCoord().getX());
			selectPlant.setInt(2, chunkData.getChunkCoord().getZ());
			selectPlant.setShort(3, (short) chunkData.getChunkCoord().getWorldID());
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
					logicMan.initGrowthTime(plant);
					insertFunction.accept(plant);
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to load plant from db: ", e);
		}
	}

}
