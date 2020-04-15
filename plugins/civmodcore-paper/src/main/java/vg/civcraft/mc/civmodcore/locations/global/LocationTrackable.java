package vg.civcraft.mc.civmodcore.locations.global;

import org.bukkit.Location;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.CacheState;

public abstract class LocationTrackable {
	
	private Location location;
	private CacheState cacheState;
	
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
	
	public void setCacheState(CacheState state) {
		this.cacheState = this.cacheState.progress(state);
	}
	
	public void setDirty() {
		setCacheState(CacheState.MODIFIED);
	}

}
