package vg.civcraft.mc.civmodcore.locations.chunkmeta.block;

import org.bukkit.Location;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.CacheState;

public abstract class BlockDataObject<D extends BlockDataObject<D>> {

	private BlockBasedChunkMeta<D, ? extends StorageEngine> owningCache;
	protected final Location location;
	
	public BlockDataObject(Location location) {
		if (location == null) {
			throw new IllegalArgumentException("Location for BlockDataObject can not be null");
		}
		this.location = location;
	}
	
	public abstract void delete();
	
	public Location getLocation() {
		return location;
	}
	
	protected BlockBasedChunkMeta<D, ? extends StorageEngine> getOwningCache() {
		return owningCache;
	}
	
	public void setCacheState(CacheState state) {
		if (state == CacheState.MODIFIED) {
			owningCache.setCacheState(state);
		}
	}
	
	public void setOwningCache(BlockBasedChunkMeta<D, ? extends StorageEngine> owningCache) {
		this.owningCache = owningCache;
	}

}
