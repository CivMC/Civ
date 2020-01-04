package com.untamedears.realisticbiomes.model;

import com.untamedears.realisticbiomes.RealisticBiomes;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedBlockChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableStorageEngine;
import vg.civcraft.mc.civmodcore.util.progress.ProgressTrackable;
import vg.civcraft.mc.civmodcore.util.progress.ProgressTracker;

public class RBChunkCache extends TableBasedBlockChunkMeta<Plant> implements ProgressTrackable {

	private ProgressTracker<Plant> tracker;
	private long nextUpdate;

	public RBChunkCache(boolean isNew, TableStorageEngine<Plant> storage) {
		super(isNew, storage);
		tracker = new ProgressTracker<>();
		this.nextUpdate = Long.MAX_VALUE;
	}

	@Override
	public int compareTo(ProgressTrackable o) {
		return getChunkCoord().compareTo(((RBChunkCache) o).getChunkCoord());
	}

	@Override
	public long getNextUpdate() {
		return nextUpdate;
	}

	@Override
	protected TableBasedDataObject remove(int x, int y, int z) {
		Plant data = (Plant) super.remove(x, y, z);
		tracker.removeItem(data);
		return data;
	}

	@Override
	public void remove(TableBasedDataObject blockData) {
		super.remove(blockData);
		tracker.removeItem((Plant) blockData);
	}

	/**
	 * Updates the growth time for the given plant in the scheduled tracking
	 * 
	 * @param plant Plant to update time for
	 * @param time  Time when the plant should next be updated
	 */
	public void updateGrowthTime(Plant plant, long time) {
		if (time < nextUpdate) {
			RealisticBiomes.getInstance().getPlantProgressManager().updateTime(this, time);
		}
		tracker.updateItem(plant, time);
	}

	@Override
	public void updateInternalProgressTime(long update) {
		this.nextUpdate = update;
	}

	@Override
	public void updateState() {
		this.nextUpdate = tracker.processItems();
	}
}
