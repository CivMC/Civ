package com.untamedears.jukealert;

import com.untamedears.jukealert.model.appender.DormantCullingAppender;
import org.bukkit.Bukkit;
import vg.civcraft.mc.civmodcore.util.progress.ProgressTracker;

public class SnitchCullManager {

	private ProgressTracker<DormantCullingAppender> tracker;

	public SnitchCullManager() {
		tracker = new ProgressTracker<>();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(JukeAlert.getInstance(), tracker::processItems, 1L, 1L);
	}
	
	public void addCulling(DormantCullingAppender cull) {
		tracker.addItem(cull);
	}
	
	public void updateCulling(DormantCullingAppender cull, long timer) {
		tracker.updateItem(cull, timer);
	}
	
	public void removeCulling(DormantCullingAppender cull) {
		tracker.removeItem(cull);
	}
}
