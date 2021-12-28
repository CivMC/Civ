package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block;

import org.bukkit.Location;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.CacheState;

public abstract class BlockDataObject<D extends BlockDataObject<D>> {

	private BlockBasedChunkMeta<D, ? extends StorageEngine> owningCache;
	protected final Location location;
	protected CacheState state;

	public BlockDataObject(Location location, boolean isNew) {
		if (location == null) {
			throw new IllegalArgumentException("Location for BlockDataObject can not be null");
		}
		this.location = location;
		this.state = isNew ? CacheState.NEW : CacheState.NORMAL;
	}

	public Location getLocation() {
		return location;
	}

	public BlockBasedChunkMeta<D, ? extends StorageEngine> getOwningCache() {
		return owningCache;
	}

	public void setCacheState(CacheState state) {
		CacheState oldState = this.state;
		this.state = this.state.progress(state);
		if (this.state != CacheState.NORMAL && oldState != this.state && owningCache != null) {
			owningCache.setCacheState(CacheState.MODIFIED);
		}
	}
	
	public CacheState getCacheState() {
		return state;
	}

	public void setOwningCache(BlockBasedChunkMeta<D, ? extends StorageEngine> owningCache) {
		this.owningCache = owningCache;
	}

}
