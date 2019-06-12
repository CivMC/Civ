package com.untamedears.realisticbiomes.model;

import org.bukkit.Location;

public class Plant {
	
	private long creationTime;
	private final Location location;
	private boolean isNew;
	private boolean isDirty;
	private boolean isDeleted;
	private long nextUpdate;
	
	private ChunkCache owningCache;
	
	public Plant(Location location) {
		this(System.currentTimeMillis(), location, true, true);
	}
	
	public Plant(long creationTime, Location location, boolean isNew, boolean dirty) {
		this.creationTime = creationTime;
		this.location = location;
		this.isDirty = dirty;
		this.isNew = isNew;
	}
	
	public void delete() {
		this.isDirty = true;
		this.isDeleted = true;
	}
	
	/**
	 * @return Creation time as unix time stamp
	 */
	public long getCreationTime() {
		return creationTime;
	}
	
	/**
	 * @return Where is the plant
	 */
	public Location getLocation() {
		return location;
	}
	
	public long getNextGrowthTime() {
		return nextUpdate;
	}
	
	public void innerUpdateGrowthTime(long time) {
		this.nextUpdate = time;
	}
	
	public boolean isDeleted() {
		return isDeleted;
	}
	
	public boolean isDirty() {
		return isDirty;
	}
	
	public boolean isNew() {
		return isNew;
	}
	
	public void setDirty(boolean dirty) {
		this.isDirty = dirty;
		if (!isDirty) {
			isNew = false;
		}
		else {
			owningCache.setDirty(true);
		}
	}
	
	public void setNextGrowthTime(long time) {
		this.owningCache.updateNextGrowthTime(this, time);
	}
	
	public void setOwningCache(ChunkCache cache) {
		this.owningCache = cache;
	}

}
