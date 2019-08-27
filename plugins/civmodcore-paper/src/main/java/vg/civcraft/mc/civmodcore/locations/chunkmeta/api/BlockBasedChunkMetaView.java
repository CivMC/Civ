package vg.civcraft.mc.civmodcore.locations.chunkmeta.api;

import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.ChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.GlobalChunkMetaManager;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockBasedChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockDataObject;

/**
 * API view for block based chunk metas, which adds convenience methods for
 * directly editing individual block data
 *
 * @param <T> BlockBasedChunkMeta subclass
 * @param <D> BlockDataObject subclass
 */
public class BlockBasedChunkMetaView<T extends BlockBasedChunkMeta<D, ?>, D extends BlockDataObject<D>>
		extends ChunkMetaView<T> {
	
	private Supplier<T> chunkProducer;

	BlockBasedChunkMetaView(JavaPlugin plugin, int pluginID, GlobalChunkMetaManager globalManager, Supplier<T> chunkProducer) {
		super(plugin, pluginID, globalManager);
		this.chunkProducer = chunkProducer;
	}

	/**
	 * Gets the data at the given location
	 * 
	 * @param block Block to get data for
	 * @return Data tied to the given block or null if no data exists there
	 */
	public D get(Block block) {
		T chunk = super.getChunkMeta(block.getLocation());
		if (chunk == null) {
			return null;
		}
		return chunk.get(block);
	}

	/**
	 * Gets the data at the given location
	 * 
	 * @param location Location to get data for
	 * @return Data at the given location or null if no data exists there
	 */
	public D get(Location location) {
		T chunk = super.getChunkMeta(location);
		if (chunk == null) {
			return null;
		}
		return chunk.get(location);
	}

	@SuppressWarnings("unchecked")
	private T getOrCreateChunkMeta(World world, int x, int z) {
		return super.computeIfAbsent(world, x, z, (Supplier<ChunkMeta<?>>)(Supplier<?>)chunkProducer);
	}

	/**
	 * Inserts data into the cache
	 * 
	 * @param data Data to insert
	 */
	public void put(D data) {
		Location loc = data.getLocation();
		T chunk = getOrCreateChunkMeta(loc.getWorld(), loc.getBlockX() / 16, loc.getBlockZ() / 16);
		chunk.put(loc, data);
	}

	/**
	 * Attempts to remove data tied to the given block from the cache, if any exists
	 * 
	 * @param block Block to remove data for
	 * @return Data removed, null if nothing was removed
	 */
	public D remove(Block block) {
		T chunk = super.getChunkMeta(block.getLocation());
		if (chunk == null) {
			throw new IllegalArgumentException("No data loaded for the chunk at location " + block.getLocation());
		}
		return chunk.remove(block);
	}

	/**
	 * Removes the given data from the cache. Data must actually be in the cache
	 * 
	 * @param data Data to remove
	 */
	public void remove(D data) {
		T chunk = super.getChunkMeta(data.getLocation());
		if (chunk == null) {
			throw new IllegalArgumentException("No data loaded for the chunk at location " + data.getLocation());
		}
		chunk.remove(data);
	}

	/**
	 * Attempts to remove data at the given location from the cache, if any exists
	 * 
	 * @param location Location to remove data from
	 * @return Data removed, null if nothing was removed
	 */
	public D remove(Location location) {
		T chunk = super.getChunkMeta(location);
		if (chunk == null) {
			throw new IllegalArgumentException("No data loaded for the chunk at location " + location);
		}
		return chunk.remove(location);
	}

}
