package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import org.bukkit.World;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.StorageEngine;

/**
 * Represents data for one specific chunk for one specific plugin.
 * 
 * Subclasses must implement their own static deserialization method as shown in
 * this class
 * 
 * None of the methods specified here should be called by anything outside of
 * this package, except for setDirty(true)
 * 
 */
public abstract class ChunkMeta<S extends StorageEngine> {

	protected int pluginID;
	protected World world;
	protected final S storage;
	protected ChunkCoord chunkCoord;

	private CacheState cacheState;

	/**
	 * 
	 * @param isNew Whether this instance is new or if it has been saved to the
	 *              database before. Should always be true for any instanciations
	 *              outside of this package
	 */
	public ChunkMeta(boolean isNew, S storage) {
		this.cacheState = isNew ? CacheState.NEW : CacheState.NORMAL;
		this.storage = storage;
	}

	/**
	 * Deletes the instances data from the storage
	 * 
	 */
	public abstract void delete();

	/**
	 * @return Whether this instance has data changed since it was last synced with
	 *         the database and needs to be written back there
	 */
	public CacheState getCacheState() {
		return cacheState;
	}
	
	/**
	 * @return ChunkCoord describing where this instance is
	 */
	public ChunkCoord getChunkCoord() {
		return chunkCoord;
	}

	/**
	 * Gets the id of the plugin to which plugin this instance belongs to
	 */
	int getPluginID() {
		return pluginID;
	}

	/**
	 * @return World this cache is in
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * Inserts this instances data into the storage *
	 * 
	 */
	public abstract void insert();

	/**
	 * Instances may be filled with data and emptied later on without the instance
	 * ever being explicitly deleted. This method is used to cull these empty
	 * metadata objects
	 * 
	 * @return Is this instance void of any data which needs to be persisted
	 */
	public abstract boolean isEmpty();

	/**
	 * Loads this instances data from the storage engine
	 * 
	 */
	public abstract void populate();

	/**
	 * Sets the cache state, which specifies whether this instance has changed since
	 * it was last synced with the database and needs to be written back there
	 * 
	 * @param state New dirty state
	 */
	public void setCacheState(CacheState state) {
		this.cacheState = this.cacheState.progress(state);
	}

	void setChunkCoord(ChunkCoord chunk) {
		this.chunkCoord = chunk;
	}

	/**
	 * Sets the id of the plugin to which plugin this instance belongs to
	 */
	void setPluginID(int pluginID) {
		this.pluginID = pluginID;
	}

	/**
	 * Sets the world this cache is in
	 * 
	 * @param world World the cache is in
	 */
	void setWorld(World world) {
		this.world = world;
	}

	/**
	 * Updates the instances data in the storage
	 */
	public abstract void update();

}
