package com.untamedears.realisticbiomes.model;

import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.model.time.ProgressTrackable;
import com.untamedears.realisticbiomes.model.time.ProgressTracker;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedBlockChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableStorageEngine;

public class RBChunkCache extends TableBasedBlockChunkMeta<Plant> implements ProgressTrackable{
	
	private ProgressTracker<Plant> tracker;
	private long nextUpdate;

	public RBChunkCache(boolean isNew, TableStorageEngine<Plant> storage) {
		super(isNew, storage);
		tracker = new ProgressTracker<>();
		this.nextUpdate = Long.MAX_VALUE;
	}
	
	public void updateGrowthTime(Plant plant, long time) {
		if (time < nextUpdate) {
			RealisticBiomes.getInstance().getPlantProgressManager().updateTime(this, time);
		}
		tracker.updateItem(plant, time);
	}

	@Override
	public long getNextUpdate() {
		return nextUpdate;
	}

	@Override
	public void updateInternalProgressTime(long update) {
		this.nextUpdate = update;
	}

	@Override
	public int compareTo(ProgressTrackable o) {
		return getChunkCoord().compareTo(((RBChunkCache) o).getChunkCoord());
	}

	@Override
	public void updateState() {
		this.nextUpdate = tracker.processItems();
	}
}
