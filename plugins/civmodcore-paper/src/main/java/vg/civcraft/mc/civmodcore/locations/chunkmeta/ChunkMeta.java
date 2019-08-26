package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import org.bukkit.World;

import com.google.gson.JsonObject;

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
public abstract class ChunkMeta {

	/**
	 * Subclasses need to implement this method to create instances of themselves
	 * when loaded from the database
	 * 
	 * @param json JsonObject to deserialize from
	 * @return Newly created instance
	 */
	public static ChunkMeta deserialize(JsonObject json) {
		throw new IllegalAccessError();
	}
	private int pluginID;
	private boolean isDirty;
	private World world;

	private boolean isNew;

	/**
	 * 
	 * @param isNew Whether this instance is new or if it has been saved to the
	 *              database before. Should always be true for any instanciations
	 *              outside of this package
	 */
	public ChunkMeta(boolean isNew) {
		this.isDirty = isNew;
		this.isNew = isNew;
	}

	/**
	 * Gets the id of the plugin to which plugin this instance belongs to
	 */
	int getPluginID() {
		return pluginID;
	}

	/**
	 * @return Whether this instance has data changed since it was last synced with
	 *         the database and needs to be written back there
	 */
	boolean isDirty() {
		return isDirty;
	}

	/**
	 * Instances may be filled with data and emptied later on without the instance
	 * ever being explicitly deleted. This method is used to cull these empty
	 * metadata objects
	 * 
	 * @return Is this instance void of any data which needs to be persisted
	 */
	abstract boolean isEmpty();

	/**
	 * @return Whether this instance is new, meaning it has never been written to
	 *         the database
	 */
	boolean isNew() {
		return isNew;
	}
	
	/**
	 * @return World this cache is in
	 */
	public World getWorld() {
		return world;
	}

	abstract JsonObject serialize();

	/**
	 * Sets the dirty flag, which specifies whether this instance has changed since
	 * it was last synced with the database and needs to be written back there
	 * 
	 * @param dirty New dirty state
	 */
	void setDirty(boolean dirty) {
		this.isDirty = dirty;
		if (!dirty && isNew) {
			isNew = false;
		}
	}

	/**
	 * Sets the id of the plugin to which plugin this instance belongs to
	 */
	void setPluginID(int pluginID) {
		this.pluginID = pluginID;
	}
	
	/**
	 * Sets the world this cache is in
	 * @param world World the cache is in
	 */
	void setWorld(World world) {
		this.world = world;
	}

}
