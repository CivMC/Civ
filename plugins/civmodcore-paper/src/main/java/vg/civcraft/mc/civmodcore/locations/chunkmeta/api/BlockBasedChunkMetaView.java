package vg.civcraft.mc.civmodcore.locations.chunkmeta.api;

import java.util.function.Supplier;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.ChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.GlobalChunkMetaManager;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.XZWCoord;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockBasedChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockBasedStorageEngine;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockDataObject;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.fallback.SingleBlockTracker;
import vg.civcraft.mc.civmodcore.locations.global.WorldIDManager;

/**
 * API view for block based chunk metas, which adds convenience methods for
 * directly editing individual block data
 *
 * @param <T> BlockBasedChunkMeta subclass
 * @param <D> BlockDataObject subclass
 */
public class BlockBasedChunkMetaView<T extends BlockBasedChunkMeta<D, S>, D extends BlockDataObject<D>, S extends BlockBasedStorageEngine<D>>
		extends ChunkMetaView<T> {

	private Supplier<T> chunkProducer;
	private S storageEngine;
	private SingleBlockTracker<D> singleBlockTracker;
	private boolean allowAccessUnloaded;
	private WorldIDManager worldIdManager;

	BlockBasedChunkMetaView(JavaPlugin plugin, short pluginID, GlobalChunkMetaManager globalManager,
			Supplier<T> chunkProducer, S storage, boolean loadAll, boolean allowAccessUnloaded) {
		super(plugin, pluginID, globalManager, loadAll);
		this.chunkProducer = chunkProducer;
		this.allowAccessUnloaded = allowAccessUnloaded;
		this.storageEngine = storage;
		if (loadAll) {
			loadAll();
		}
		worldIdManager = CivModCorePlugin.getInstance().getWorldIdManager();
		singleBlockTracker = new SingleBlockTracker<>();
	}

	private void loadAll() {
		for (XZWCoord coord : storageEngine.getAllDataChunks()) {
			getOrCreateChunkMeta(worldIdManager.getWorldByInternalID(pluginID), coord.getX(), coord.getZ());
		}
	}

	/**
	 * Gets the data at the given location
	 * 
	 * @param block Block to get data for
	 * @return Data tied to the given block or null if no data exists there
	 */
	public D get(Block block) {
		return get(block.getLocation());
	}

	/**
	 * Gets the data at the given location
	 * 
	 * @param location Location to get data for
	 * @return Data at the given location or null if no data exists there
	 */
	public D get(Location location) {
		validateY(location.getBlockY());
		short worldID = worldIdManager.getInternalWorldId(location.getWorld());
		T chunk = super.getChunkMeta(location);
		D data;
		if (chunk == null) {
			if (alwaysLoaded) {
				return null;
			}
			if (!allowAccessUnloaded) {
				throw new IllegalStateException("Can not load data for unloaded chunk");
			}
			data = singleBlockTracker.getBlock(location, worldID);
			if (data == null) {
				data = storageEngine.getForLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(),
					worldID, pluginID);
			}
			if (data != null) {
				singleBlockTracker.putBlock(data, worldID);
			}
		} else {
			return chunk.get(location);
		}
		return data;
	}

	@SuppressWarnings("unchecked")
	private T getOrCreateChunkMeta(World world, int x, int z) {
		return super.computeIfAbsent(world, x, z, (Supplier<ChunkMeta<?>>) (Supplier<?>) chunkProducer);
	}

	/**
	 * Inserts data into the cache
	 * 
	 * @param data Data to insert
	 */
	public void put(D data) {
		if (data == null) {
			throw new IllegalArgumentException("Data to insert can not be null");
		}
		Location loc = data.getLocation();
		validateY(loc.getBlockY());
		T chunk;
		if (alwaysLoaded) {
			chunk = getOrCreateChunkMeta(loc.getWorld(), loc.getChunk().getX(), loc.getChunk().getZ());
		} else {
			chunk = super.getChunkMeta(loc.getWorld(), loc.getChunk().getX(), loc.getChunk().getZ());
		}
		if (chunk != null) {
			chunk.put(loc, data);
			return;
		}
		if (!allowAccessUnloaded) {
			throw new IllegalStateException("Can not insert data for unloaded chunk");
		}
		singleBlockTracker.putBlock(data, worldIdManager.getInternalWorldId(loc.getWorld()));

	}

	/**
	 * Attempts to remove data tied to the given block from the cache, if any exists
	 * 
	 * @param block Block to remove data for
	 * @return Data removed, null if nothing was removed
	 */
	public D remove(Block block) {
		return remove(block.getLocation());
	}

	/**
	 * Removes the given data from the cache. Data must actually be in the cache
	 * 
	 * @param data Data to remove
	 */
	public void remove(D data) {
		D removed = remove(data.getLocation());
		if (removed != data) {
			throw new IllegalStateException("Removed data non-identical to the one supposed to be removed");
		}
	}

	/**
	 * Attempts to remove data at the given location from the cache, if any exists
	 * 
	 * @param location Location to remove data from
	 * @return Data removed, null if nothing was removed
	 */
	public D remove(Location location) {
		validateY(location.getBlockY());
		T chunk = super.getChunkMeta(location);
		if (chunk != null) {
			return chunk.remove(location);
		}
		if (alwaysLoaded) {
			return null;
		}
		if (!allowAccessUnloaded) {
			throw new IllegalStateException("Can not delete data for unloaded chunk");
		}
		return singleBlockTracker.removeBlock(location, worldIdManager.getInternalWorldId(location.getWorld()));

	}

	private static void validateY(int y) {
		if (y < 0) {
			throw new IllegalArgumentException("Y-level of data may not be less than 0");
		}
		if (y > 255) {
			throw new IllegalArgumentException("Y-level of data may not be more than 255");
		}
	}

	@Override
	public void postLoad(ChunkMeta<?> c) {
		@SuppressWarnings("unchecked")
		T chunk = (T) c;
		for (D data : singleBlockTracker.getAllForChunkAndRemove(chunk.getChunkCoord())) {
			chunk.put(BlockBasedChunkMeta.modulo(data.getLocation().getBlockX()), data.getLocation().getBlockY(),
					BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ()), data, true, false);
		}
	}

	@Override
	public void disable() {
		for (D data : singleBlockTracker.getAll()) {
			storageEngine.persist(data, worldIdManager.getInternalWorldId(data.getLocation().getWorld()), pluginID);
		}
		super.disable();
	}

}
