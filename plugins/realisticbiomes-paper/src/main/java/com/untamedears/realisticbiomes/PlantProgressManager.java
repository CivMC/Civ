package com.untamedears.realisticbiomes;

import com.untamedears.realisticbiomes.model.RBChunkCache;
import org.bukkit.Bukkit;
import vg.civcraft.mc.civmodcore.utilities.progress.ProgressTracker;

/**
 * Keeps track of which plant needs to be updated next
 *
 */
public class PlantProgressManager {

	private ProgressTracker<RBChunkCache> tracker;

	public PlantProgressManager() {
		this.tracker = new ProgressTracker<>();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(RealisticBiomes.getInstance(), this::processUpdates, 1L, 1L);
	}

	public void addChunk(RBChunkCache cache) {
		tracker.addItem(cache);
	}

	public void processUpdates() {
		tracker.processItems();
	}

	public void removeChunk(RBChunkCache cache) {
		tracker.removeItem(cache);
	}

	public void updateTime(RBChunkCache cache, long time) {
		tracker.updateItem(cache, time);
		cache.updateInternalProgressTime(time);
	}

}
