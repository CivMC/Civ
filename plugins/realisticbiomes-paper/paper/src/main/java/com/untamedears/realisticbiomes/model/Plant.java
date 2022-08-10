package com.untamedears.realisticbiomes.model;

import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import org.bukkit.Location;
import org.bukkit.block.Block;
import vg.civcraft.mc.civmodcore.utilities.BukkitComparators;
import vg.civcraft.mc.civmodcore.utilities.progress.ProgressTrackable;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.CacheState;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableBasedDataObject;

import java.util.Date;

public class Plant extends TableBasedDataObject implements ProgressTrackable {

	private long creationTime;
	private long nextUpdate;
	private PlantGrowthConfig growthConfig;

	public Plant(Location location, PlantGrowthConfig plantType) {
		this(System.currentTimeMillis(), location, true, plantType);
	}

	public Plant(long creationTime, Location location, boolean isNew, PlantGrowthConfig growthConfig) {
		super(location, isNew);
		this.creationTime = creationTime;
		this.growthConfig = growthConfig;
	}

	public PlantGrowthConfig getGrowthConfig() {
		return growthConfig;
	}

	public void setGrowthConfig(PlantGrowthConfig growthConfig) {
		if (growthConfig != this.growthConfig) {
			this.growthConfig = growthConfig;
			setDirty();
		}
	}

	@Override
	public int compareTo(ProgressTrackable o) {
		return BukkitComparators.getLocation().compare(getLocation(), ((Plant) o).getLocation());
	}

	/**
	 * @return Creation time as unix time stamp
	 */
	public long getCreationTime() {
		return creationTime;
	}

	@Override
	public long getNextUpdate() {
		return nextUpdate;
	}

	public void resetCreationTime() {
		setCreationTime(System.currentTimeMillis());
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
		setDirty();
	}

	/**
	 * Use this method to set the next update, not setNextUpdate()
	 * 
	 * @param time
	 */
	public void setNextGrowthTime(long time) {
		((RBChunkCache) getOwningCache()).updateGrowthTime(this, time);
	}

	/**
	 * Internal method, don't use this
	 */
	@Override
	public void updateInternalProgressTime(long update) {
		this.nextUpdate = update;
	}

	@Override
	public void updateState() {
		if (getCacheState() == CacheState.DELETED) {
			nextUpdate = Long.MAX_VALUE;
			return;
		}
		if (growthConfig != null) {
			nextUpdate = growthConfig.updatePlant(this);
		} else {
			Block block = location.getBlock();
			PlantGrowthConfig newConfig = RealisticBiomes.getInstance().getGrowthConfigManager()
					.getGrowthConfigFallback(block.getType());
			if (newConfig != null) {
				setGrowthConfig(newConfig);
				nextUpdate = newConfig.updatePlant(this);
			}
			else {
				nextUpdate = Long.MAX_VALUE;
			}
		}
	}
	
	public String toString() {
		return String.format("Created: %s, Loc: %s, Next update in: %s, config: %s",
				new Date(creationTime),
				"[" + getLocation().getBlockX() + " " + getLocation().getBlockY() + " " + getLocation().getBlockZ() + "]",
				nextUpdate != Long.MAX_VALUE ? (nextUpdate - System.currentTimeMillis()) + " ms" : "Never",
				growthConfig
		);
	}
}
