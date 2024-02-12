package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.auto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.World;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.ChunkCoord;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.XZWCoord;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.BlockBasedChunkMeta;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.BlockBasedStorageEngine;

public class AutoStorageEngine<D extends SerializableDataObject<D>> implements BlockBasedStorageEngine<D> {

	private ManagedDatasource db;
	private Logger logger;
	private BiFunction<Location, String, D> dataDeserializer;

	public AutoStorageEngine(ManagedDatasource db, Logger logger,
			BiFunction<Location, String, D> dataDeserializer) {
		this.db = db;
		this.logger = logger;
		this.dataDeserializer = dataDeserializer;
	}

	protected void deleteData(short pluginID, D data) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement deleteChunk = insertConn.prepareStatement(
						"delete from cmc_chunk_data where chunk_x = ? and chunk_z = ? and world_id = ? and plugin_id = ? and x_offset = ? and y = ? and z_offset = ?;")) {
			ChunkCoord chunkCoord = data.getOwningCache().getChunkCoord();
			deleteChunk.setInt(1, chunkCoord.getX());
			deleteChunk.setInt(2, chunkCoord.getZ());
			deleteChunk.setShort(3, chunkCoord.getWorldID());
			deleteChunk.setShort(4, pluginID);
			deleteChunk.setByte(5, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockX()));
			deleteChunk.setShort(6, (short) data.getLocation().getBlockY());
			deleteChunk.setByte(7, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ()));
			deleteChunk.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to delete chunk data", e);
		}
	}

	protected void insertData(short pluginID, D data) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement insertChunk = insertConn.prepareStatement(
						"insert into cmc_chunk_data (chunk_x, chunk_z, world_id, plugin_id, x_offset, y, z_offset, data) values(?,?,?,?,?,?,?,?)")) {
			ChunkCoord chunkCoord = data.getOwningCache().getChunkCoord();
			insertChunk.setInt(1, chunkCoord.getX());
			insertChunk.setInt(2, chunkCoord.getZ());
			insertChunk.setShort(3, chunkCoord.getWorldID());
			insertChunk.setShort(4, pluginID);
			insertChunk.setByte(5, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockX()));
			insertChunk.setShort(6, (short) data.getLocation().getBlockY());
			insertChunk.setByte(7, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ()));
			insertChunk.setString(8, data.serialize().toString());
			insertChunk.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to insert chunk data", e);
		}
	}

	public void loadDataForChunk(short pluginID, ChunkCoord coord, Consumer<D> applyFunction) {
		int preMultipliedX = coord.getX() * 16;
		int preMultipliedZ = coord.getZ() * 16;
		try (Connection insertConn = db.getConnection();
				PreparedStatement getData = insertConn.prepareStatement(
						"select x_offset, y, z_offset, data from cmc_chunk_data where chunk_x = ? and chunk_z = ? and world_id = ? and plugin_id = ?;")) {
			getData.setInt(1, coord.getX());
			getData.setInt(2, coord.getZ());
			getData.setShort(3, coord.getWorldID());
			getData.setShort(4, pluginID);
			try (ResultSet rs = getData.executeQuery()) {
				while (rs.next()) {
					int xOffset = rs.getByte(1);
					int x = xOffset + preMultipliedX;
					int y = rs.getShort(2);
					int zOffset = rs.getByte(3);
					int z = zOffset + preMultipliedZ;
					Location loc = new Location(coord.getWorld(), x, y, z);
					String rawData = rs.getString(4);
					D data = dataDeserializer.apply(loc,rawData);
					if (data != null) {
						applyFunction.accept(data);
					}
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to load chunk data", e);
			// we want to escalate this, this is really bad
			throw new IllegalStateException("Failed to load chunk data");
		}
	}

	protected void updateData(short pluginID, D data) {
		try (Connection insertConn = db.getConnection();
				PreparedStatement updateChunk = insertConn.prepareStatement(
						"update cmc_chunk_data set data = ? where chunk_x = ? and z = ? and world_id = ? and plugin_id = ? and x_offset = ? and y = ? and z_offset = ?;")) {
			ChunkCoord chunkCoord = data.getOwningCache().getChunkCoord();
			updateChunk.setString(1, data.serialize().toString());
			updateChunk.setInt(2, chunkCoord.getX());
			updateChunk.setInt(3, chunkCoord.getZ());
			updateChunk.setShort(4, chunkCoord.getWorldID());
			updateChunk.setShort(5, pluginID);
			updateChunk.setByte(6, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockX()));
			updateChunk.setShort(7, (short) data.getLocation().getBlockY());
			updateChunk.setByte(8, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ()));
			updateChunk.execute();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to update chunk data", e);
		}
	}

	@Override
	public List<XZWCoord> getAllDataChunks() {
		List<XZWCoord> result = new ArrayList<>();
		try (Connection insertConn = db.getConnection();
				PreparedStatement getChunks = insertConn.prepareStatement(
						"select x, z, world_id from cmc_chunk_data group by chunk_x, chunk_z, world_id;");
				ResultSet rs = getChunks.executeQuery()) {
			while (rs.next()) {
				int x = rs.getInt(1);
				int z = rs.getInt(2);
				short worldID = rs.getShort(3);
				result.add(new XZWCoord(x, z, worldID));
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to retrieve chunk data", e);
		}
		return result;
	}

	@Override
	public boolean stayLoaded() {
		return false;
	}

	@Override
	public D getForLocation(int x, int y, int z, short worldID, short pluginID) {
		int chunkX = BlockBasedChunkMeta.toChunkCoord(x);
		int chunkZ = BlockBasedChunkMeta.toChunkCoord(z);
		try (Connection insertConn = db.getConnection();
				PreparedStatement selectRein = insertConn.prepareStatement(
						"select data from cmc_chunk_data where chunk_x = ? and chunk_z = ? and world_id = ? and plugin_id = ? and x_offset = ? and y = ? and z_offset = ?;");) {
			selectRein.setInt(1, chunkX);
			selectRein.setInt(2, chunkZ);
			selectRein.setShort(3, worldID);
			selectRein.setByte(4, (byte) BlockBasedChunkMeta.modulo(x));
			selectRein.setShort(5, (short) y);
			selectRein.setByte(6, (byte) BlockBasedChunkMeta.modulo(z));
			try (ResultSet rs = selectRein.executeQuery()) {
				if (!rs.next()) {
					return null;
				}
				World world = CivModCorePlugin.getInstance().getWorldIdManager().getWorldByInternalID(worldID);
				Location loc = new Location(world, x, y, z);
				String rawData = rs.getString(1);
				return dataDeserializer.apply(loc, rawData);
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to load jsoned data from db: ", e);
			return null;
		}
	}

	@Override
	public void persist(D data, short worldID, short pluginID) {
		switch (data.getCacheState()) {
		case DELETED:

			break;
		case MODIFIED:
			break;
		case NEW:
			break;
		case NORMAL:
			break;
		default:
			break;

		}
	}

}
