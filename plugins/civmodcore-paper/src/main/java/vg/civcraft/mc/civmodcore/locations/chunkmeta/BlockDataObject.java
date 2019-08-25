package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import org.bukkit.Location;

import com.google.gson.JsonObject;

public abstract class BlockDataObject  {

	private BlockBasedChunkMeta<? extends BlockDataObject> owningCache;
	protected final Location location;
	
	public BlockDataObject(Location location) {
		if (location == null) {
			throw new IllegalArgumentException("Location for BlockDataObject can not be null");
		}
		this.location = location;
	}
	
	public Location getLocation() {
		return location;
	}
	
	void setOwningCache(BlockBasedChunkMeta<? extends BlockDataObject> owningCache) {
		this.owningCache = owningCache;
	}
	
	public void setDirty(boolean dirty) {
		if (dirty) {
			owningCache.setDirty(true);
		}
	}
	
	protected BlockBasedChunkMeta<? extends BlockDataObject> getOwningCache() {
		return owningCache;
	}

	public static BlockDataObject deserialize(JsonObject json) {
		throw new IllegalAccessError();
	}

	public abstract JsonObject serialize();

}
