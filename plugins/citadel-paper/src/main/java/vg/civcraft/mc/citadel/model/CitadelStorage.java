package vg.civcraft.mc.citadel.model;

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

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementTypeManager;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.ChunkCoord;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockBasedChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedBlockChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableStorageEngine;

public class CitadelStorage extends TableStorageEngine<Reinforcement> {

	public CitadelStorage(Logger logger, ManagedDatasource db) {
		super(logger, db);
	}

	@Override
	public void registerMigrations() {
		db.registerMigration(15, false,
				"create table ctdl_reinforcements (chunk_x int not null, chunk_z int not null, world_id smallint unsigned not null, "
						+ "x_offset tinyint unsigned not null, y tinyint unsigned not null, z_offset tinyint unsigned not null, "
						+ "type_id smallint unsigned not null, health float not null, group_id int not null, insecure boolean not null default false,"
						+ "creation_time timestamp not null default now(), index reinChunkLookUp(chunk_x, chunk_z, world_id), primary key "
						+ "(chunk_x, chunk_z, world_id, x_offset, y ,z_offset))");
	}

	@Override
	public void insert(Reinforcement data, ChunkCoord coord) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement insertRein = insertConn.prepareStatement(
						"insert into ctdl_reinforcements (chunk_x, chunk_z, world_id, x_offset, y, z_offset, type_id, "
								+ "health, group_id, insecure, creation_time) values(?,?,?, ?,?,?, ?,?,?,?,?);");) {
			insertRein.setInt(1, coord.getX());
			insertRein.setInt(2, coord.getZ());
			insertRein.setShort(3, (short) coord.getWorldID());
			insertRein.setByte(4, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockX(), 16));
			insertRein.setByte(5, (byte) data.getLocation().getBlockY());
			insertRein.setByte(6, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ(), 16));
			insertRein.setShort(7, data.getType().getID());
			insertRein.setFloat(8, data.getHealth());
			insertRein.setInt(9, data.getGroupId());
			insertRein.setBoolean(10, data.isInsecure());
			insertRein.setTimestamp(11, new Timestamp(data.getCreationTime()));
			insertRein.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to insert reinforcement into db: ", e);
		}
	}

	@Override
	public void update(Reinforcement data, ChunkCoord coord) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement updateRein = insertConn.prepareStatement(
						"update ctdl_reinforcements set type_id = ?, health = ?, group_id = ?, insecure = ? where "
								+ "chunk_x = ? and chunk_z = ? and world_id = ? and x_offset = ? and y = ? and z_offset = ?;");) {
			updateRein.setShort(1, data.getType().getID());
			updateRein.setFloat(2, data.getHealth());
			updateRein.setInt(3, data.getGroupId());
			updateRein.setBoolean(4, data.isInsecure());
			updateRein.setInt(5, coord.getX());
			updateRein.setInt(6, coord.getZ());
			updateRein.setShort(7, (short) coord.getWorldID());
			updateRein.setByte(8, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockX(), 16));
			updateRein.setByte(9, (byte) data.getLocation().getBlockY());
			updateRein.setByte(10,(byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ(), 16));
			updateRein.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to update reinforcement in db: ", e);
		}
	}

	@Override
	public void delete(Reinforcement data, ChunkCoord coord) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement deleteRein = insertConn.prepareStatement(
						"delete from ctdl_reinforcements where chunk_x = ? and chunk_z = ? and world_id = ? and "
						+ "x_offset = ? and y = ? and z_offset = ?;");) {
			deleteRein.setInt(1, coord.getX());
			deleteRein.setInt(2, coord.getZ());
			deleteRein.setShort(3, (short) coord.getWorldID());
			deleteRein.setByte(4, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockX(), 16));
			deleteRein.setByte(5, (byte) data.getLocation().getBlockY());
			deleteRein.setByte(6, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ(), 16));
			deleteRein.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to delete reinforcement from db: ", e);
		}
	}

	@Override
	public void fill(TableBasedBlockChunkMeta<Reinforcement> chunkData, Consumer<Reinforcement> insertFunction) {
		int preMultipliedX = chunkData.getChunkCoord().getX() * 16;
		int preMultipliedZ = chunkData.getChunkCoord().getZ() * 16;
		ReinforcementTypeManager typeMan = Citadel.getInstance().getReinforcementTypeManager();
		World world = chunkData.getChunkCoord().getWorld();
		try (Connection insertConn = db.getConnection();
				PreparedStatement selectRein = insertConn.prepareStatement(
						"select x_offset, y, z_offset, type_id, group_id, creation_time, health, insecure "
								+ "from ctdl_reinforcements where chunk_x = ? and chunk_z = ? and world_id = ?;");) {
			selectRein.setInt(1, chunkData.getChunkCoord().getX());
			selectRein.setInt(2, chunkData.getChunkCoord().getZ());
			selectRein.setShort(3, (short) chunkData.getChunkCoord().getWorldID());
			try (ResultSet rs = selectRein.executeQuery()) {
				while (rs.next()) {
					int xOffset = rs.getByte(1);
					int x = xOffset + preMultipliedX;
					int y = rs.getByte(2) & 0xFF;
					int zOffset = rs.getByte(3);
					int z = zOffset + preMultipliedZ;
					Location location = new Location(world, x, y, z);
					short typeID = rs.getShort(4);
					ReinforcementType type = typeMan.getById(typeID);
					if (type == null) {
						logger.log(Level.SEVERE, "Failed to load reinforcement with type id " + typeID);
						continue;
					}
					int groupID = rs.getInt(5);
					long creationTime = rs.getTimestamp(6).getTime();
					float health = rs.getFloat(7);
					boolean insecure = rs.getBoolean(8);
					Reinforcement rein = new Reinforcement(location, type, groupID, creationTime, health, insecure,
							false);
					insertFunction.accept(rein);
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to load reinforcement from db: ", e);
		}
	}

}
