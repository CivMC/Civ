package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

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
	private Map<Integer, ChunkMeta> chunkMetas;
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

	ChunkCoord(int x, int z) {
		this.x = x;
		this.z = z;
		this.chunkMetas = new TreeMap<>();
		this.isFullyLoaded = false;
		this.lastLoadingTime = -1;
		this.lastUnloadingTime = -1;
	}

	void addChunkMeta(ChunkMeta chunkMeta) {
		chunkMetas.put(chunkMeta.getPluginID(), chunkMeta);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ChunkCoord) {
			ChunkCoord pair = (ChunkCoord) o;
			return pair.x == x && pair.z == z;
		}
		return false;
	}

	/**
	 * Writes all data held by this instance to the datavase
	 * 
	 * @param dao     DAO to write to
	 * @param worldID ID of the world this instance is in
	 */
	void fullyPersist(ChunkDAO dao, int worldID) {
		for (ChunkMeta chunkMeta : chunkMetas.values()) {
			if (!chunkMeta.isDirty()) {
				continue;
			}
			if (chunkMeta.isEmpty()) {
				if (!chunkMeta.isNew()) {
					dao.deleteChunkData(chunkMeta.getPluginID(), worldID, x, z);
				}
			} else {
				if (chunkMeta.isNew()) {
					dao.insertChunkData(chunkMeta.getPluginID(), worldID, worldID, worldID, chunkMeta);
				} else {
					dao.updateChunkData(chunkMeta.getPluginID(), worldID, worldID, worldID, chunkMeta);
				}
			}
			chunkMeta.setDirty(false);
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

	ChunkMeta getMeta(int pluginID) {
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

	int getX() {
		return x;
	}

	int getZ() {
		return z;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, z);
	}

	boolean isFullyLoaded() {
		return isFullyLoaded;
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

	/**
	 * Used by the database to tell that all data tied to this chunk has been fully
	 * loaded and to resume pending blocked data reads
	 */
	public synchronized void setFullyLoaded() {
		isFullyLoaded = true;
		notifyAll();
	}
}
