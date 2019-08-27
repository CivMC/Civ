package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Supplier;

public class ChunkCoord {

	/**
	 * When was this chunk last loaded in Minecraft as UNIX timestamp
	 */
	private long lastLoadingTime;
	/**
	 * When was this chunk last unloaded in Minecraft as UNIX timestamp
	 */
	private long lastUnloadingTime;
	/**
	 * Each ChunkMeta belongs to one plugin, they are identified by the plugin id
	 */
	private Map<Integer, ChunkMeta<?>> chunkMetas;
	/**
	 * Set to true once all data has been loaded for this chunk and stays true for
	 * the entire life time of this object
	 */
	private boolean isFullyLoaded;

	/**
	 * Chunk x-coord
	 */
	private int x;
	/**
	 * Chunk z-coord
	 */
	private int z;

	private int worldID;

	ChunkCoord(int x, int z, int worldID) {
		this.x = x;
		this.z = z;
		this.worldID = worldID;
		this.chunkMetas = new TreeMap<>();
		this.isFullyLoaded = false;
		this.lastLoadingTime = -1;
		this.lastUnloadingTime = -1;
	}

	void addChunkMeta(ChunkMeta<?> chunkMeta) {
		chunkMetas.put(chunkMeta.getPluginID(), chunkMeta);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ChunkCoord) {
			ChunkCoord pair = (ChunkCoord) o;
			return pair.x == x && pair.z == z && pair.worldID == worldID;
		}
		return false;
	}

	/**
	 * Writes all data held by this instance to the datavase
	 * 
	 * @param dao DAO to write to
	 * @pa boolean isFullyLoaded() { return isFullyLoaded; }ram worldID ID of the
	 *     world this instance is in
	 */
	void fullyPersist() {
		for (ChunkMeta<?> chunkMeta : chunkMetas.values()) {
			switch (chunkMeta.getCacheState()) {
			case NORMAL:
				break;
			case MODIFIED:
				chunkMeta.update();
				break;
			case NEW:
				chunkMeta.insert();
				break;
			case DELETED:
				chunkMeta.delete();
			}
			chunkMeta.setCacheState(CacheState.NORMAL);
		}
	}

	/**
	 * @return When was the minecraft chunk (the block data) this object is tied
	 *         last loaded (UNIX timestamp)
	 */
	long getLastMCLoadingTime() {
		return lastLoadingTime;
	}

	/**
	 * @return When was the minecraft chunk (the block data) this object is tied
	 *         last unloaded (UNIX timestamp)
	 */
	long getLastMCUnloadingTime() {
		return lastUnloadingTime;
	}

	ChunkMeta<?> getMeta(int pluginID) {
		if (!isFullyLoaded) {
			// check before taking monitor. This is fine, because the loaded flag will never
			// switch from true to false,
			// only the other way around
			synchronized (this) {
				while (!isFullyLoaded) {

					try {
						wait();
					} catch (InterruptedException e) {
						// whatever
						e.printStackTrace();
					}
				}
			}
		}
		return chunkMetas.get(pluginID);
	}

	/**
	 * @return Internal ID of the world this chunk is in
	 */
	public int getWorldID() {
		return worldID;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, z);
	}

	/**
	 * Loads data for all plugins for this chunk
	 */
	void loadAll() {
		synchronized (this) {
			if (isFullyLoaded) {
				return;
			}

			for (Entry<Integer, Supplier<ChunkMeta<?>>> generator : ChunkMetaFactory.getInstance()
					.getEmptyChunkFunctions()) {
				ChunkMeta<?> chunk = generator.getValue().get();
				chunk.setChunkCoord(this);
				chunk.setPluginID(generator.getKey());
				chunk.populate();
			}
			this.notifyAll();
			isFullyLoaded = true;
		}
	}

	/**
	 * Called when the minecraft chunk (the block data) this object is tied to gets
	 * loaded
	 */
	void minecraftChunkLoaded() {
		this.lastLoadingTime = System.currentTimeMillis();
	}

	/**
	 * Called when the minecraft chunk (the block data) this object is tied to gets
	 * unloaded
	 */
	void minecraftChunkUnloaded() {
		this.lastUnloadingTime = System.currentTimeMillis();
	}
}
