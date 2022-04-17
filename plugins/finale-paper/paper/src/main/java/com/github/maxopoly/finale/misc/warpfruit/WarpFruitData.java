package com.github.maxopoly.finale.misc.warpfruit;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.FinaleManager;
import com.google.common.collect.EvictingQueue;
import org.bukkit.Location;

public class WarpFruitData {

	private final WarpFruitTracker tracker;

	private long lastLocationLogTime = 0;
	private EvictingQueue<Location> locationsLog;

	private double animateAngle = 0;

	public WarpFruitData(WarpFruitTracker tracker) {
		this.tracker = tracker;

		locationsLog = EvictingQueue.create(tracker.getLogSize());
	}

	public void logLocation(Location loc) {
		if (System.currentTimeMillis() - lastLocationLogTime < tracker.getLogInterval()) {
			return;
		}
		locationsLog.add(loc);
		lastLocationLogTime = System.currentTimeMillis();
	}

	public Location getTimeWarpLocation() {
		return locationsLog.peek();
	}

	public void setAnimateAngle(double animateAngle) {
		this.animateAngle = animateAngle;
	}

	public double getAnimateAngle() {
		return animateAngle;
	}

}
