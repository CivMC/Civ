package vg.civcraft.mc.civmodcore.world.locations.global;

import org.bukkit.Location;
import org.bukkit.block.Block;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.CacheState;

public abstract class LocationTrackable {
	
	private Location location;
	private CacheState cacheState;
	private GlobalLocationTracker tracker;
	
	public LocationTrackable(boolean isNew, Location location) {
		this.location = location;
		cacheState = isNew ? CacheState.NEW : CacheState.NORMAL;
	}
	
	public CacheState getCacheState() {
		return cacheState;
	}
	
	public Location getLocation() {
		return location;
	}

	public void onChunkLoad(Block block) {
	}

	public void onChunkUnload(Block block) {
	}
	
	public void setCacheState(CacheState state) {
		this.cacheState = this.cacheState.progress(state);
	}
	
	public void setDirty() {
		setCacheState(CacheState.MODIFIED);

		if (tracker != null) {
			tracker.setModified(this);
		}
	}

	void setTracker(GlobalLocationTracker tracker) {
		this.tracker = tracker;
	}
}
