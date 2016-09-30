package com.devotedmc.ExilePearl.core;

import java.util.logging.Level;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.util.BukkitTask;
import com.devotedmc.ExilePearl.util.Guard;

/**
 * Interval task that deducts strength from existing prison pearls
 * @author Gordon
 */
class PearlDecayTask implements BukkitTask, Runnable {

	private final ExilePearlApi pearlApi;

	private boolean enabled = false;
	private int taskId = 0;

	// 20 * 60 ticks/ minute
	private static final int TICKS_PER_MINUTE = 1200;

	/**
	 * Creates a new FactoryWorker instance
	 */
	public PearlDecayTask(final ExilePearlApi pearlApi) {
		Guard.ArgumentNotNull(pearlApi, "pearlApi");

		this.pearlApi = pearlApi;
	}


	/**
	 * Starts the worker task
	 */
	public void start() {
		if (enabled) {
			pearlApi.log(Level.WARNING, "Tried to start the pearl worker task but it was already started.");
			return;
		}
		
		long tickInterval = pearlApi.getPearlConfig().getPearlHealthDecayIntervalMin() * TICKS_PER_MINUTE;
		taskId = pearlApi.getScheduler().scheduleSyncRepeatingTask(pearlApi.getPlugin(), this, tickInterval, tickInterval);
		if (taskId == -1) {
			pearlApi.log(Level.SEVERE, "Failed to start pearl worker task");
			return;
		} else {
			enabled = true;
			pearlApi.log("Started the pearl worker task");
		}
	}

	/**
	 * Stops the worker task
	 */
	public void stop() {
		if (enabled) {
			pearlApi.getScheduler().cancelTask(taskId);
			enabled = false;
			taskId = 0;
			pearlApi.log("Stopped the pearl worker task");
		}
	}

	/**
	 * Restarts the worker task
	 */
	public void restart() {
		stop();
		start();
	}


	@Override
	public boolean isRunning() {
		return enabled;
	}


	@Override
	public void run() {
		if (!enabled) {
			return;
		}
		
		pearlApi.decayPearls();
	}
}
