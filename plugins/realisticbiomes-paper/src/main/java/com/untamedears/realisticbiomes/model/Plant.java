package com.untamedears.realisticbiomes.model;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.model.time.ProgressTrackable;
import com.untamedears.realisticbiomes.utils.RBUtils;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.civmodcore.util.BukkitComparators;

public class Plant extends TableBasedDataObject implements ProgressTrackable {

	private long creationTime;
	private long nextUpdate;

	public Plant(Location location) {
		this(System.currentTimeMillis(), location, true);
	}

	public Plant(long creationTime, Location location, boolean isNew) {
		super(location, isNew);
		this.creationTime = creationTime;
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
		Block block = location.getBlock();
		PlantGrowthConfig growthConfig = RealisticBiomes.getInstance().getGrowthConfigManager()
				.getPlantGrowthConfig(block.getType());
		if (growthConfig == null) {
			nextUpdate = Long.MAX_VALUE;
			getOwningCache().remove(this);
			return;
		}
		if (growthConfig.isFullyGrown(this)) {
			// fruit based
			Material fruitMaterial = RBUtils.getFruit(block.getType());
			if (fruitMaterial == null) {
				nextUpdate = Long.MAX_VALUE;
				return;
			}
			growthConfig = RealisticBiomes.getInstance().getGrowthConfigManager().getPlantGrowthConfig(fruitMaterial);
			if (growthConfig != null) {
				nextUpdate = growthConfig.updatePlant(this);
				return;
			}
			nextUpdate = Long.MAX_VALUE;
		} else {
			// normal crop
			nextUpdate = growthConfig.updatePlant(this);
		}
	}
}
