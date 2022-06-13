package com.untamedears.realisticbiomes.model;

import com.untamedears.realisticbiomes.RealisticBiomes;
import org.bukkit.Bukkit;
import vg.civcraft.mc.civmodcore.utilities.progress.ProgressTrackable;
import vg.civcraft.mc.civmodcore.utilities.progress.ProgressTracker;
import vg.civcraft.mc.civmodcore.world.WorldUtils;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableBasedBlockChunkMeta;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableStorageEngine;

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
	protected Plant remove(int x, int y, int z) {
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
		if (!WorldUtils.isChunkLoaded(getWorld(), chunkCoord.getX(), chunkCoord.getZ())) {
			this.nextUpdate = Long.MAX_VALUE;
			return;
		}
		this.nextUpdate = tracker.processItems();
	}

	@Override
	public void handleChunkCacheReuse() {
		// update all plants in the chunk and reinsert them into the growth updating
		// cache
		Bukkit.getScheduler().runTask(RealisticBiomes.getInstance(), () -> iterateAll(p -> RealisticBiomes.getInstance()
				.getPlantLogicManager().updateGrowthTime((Plant) p, p.getLocation().getBlock())));
	}

	@Override
	public void handleChunkUnload() {
		Bukkit.getScheduler().runTask(RealisticBiomes.getInstance(), () -> {
			RealisticBiomes.getInstance().getPlantProgressManager().removeChunk(this);
			updateInternalProgressTime(Long.MAX_VALUE);
		});
	}
}
