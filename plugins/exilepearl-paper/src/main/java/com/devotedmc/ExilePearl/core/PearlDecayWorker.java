package com.devotedmc.ExilePearl.core;

import java.util.logging.Level;

import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.util.BukkitTask;
import com.devotedmc.ExilePearl.util.Guard;

/**
 * Interval task that deducts strength from existing prison pearls
 * @author Gordon
 */
class PearlDecayWorker implements BukkitTask, Runnable {

	private final ExilePearlPlugin plugin;

	private boolean enabled = false;
	private int taskId = 0;

	// 20 * 60 ticks/ minute
	private static final int TICKS_PER_MINUTE = 1200;

	/**
	 * Creates a new FactoryWorker instance
	 */
	public PearlDecayWorker(final ExilePearlPlugin plugin) {
		Guard.ArgumentNotNull(plugin, "plugin");

		this.plugin = plugin;
	}


	/**
	 * Starts the worker task
	 */
	public void start() {
		if (enabled) {
			plugin.log(Level.WARNING, "Tried to start the pearl worker task but it was already started.");
		}
		
		long tickInterval = plugin.getPearlConfig().getPearlHealthDecayIntervalMin() * TICKS_PER_MINUTE;
		taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, tickInterval, tickInterval);
		if (taskId == -1) {
			enabled = true;
			plugin.log(Level.SEVERE, "Failed to start pearl worker task");
		} else {
			enabled = true;
			plugin.log("Started the pearl worker task");
		}
	}

	/**
	 * Stops the worker task
	 */
	public void stop() {
		if (enabled) {
			plugin.getServer().getScheduler().cancelTask(taskId);
			enabled = false;
			taskId = 0;
			plugin.log("Stopped the pearl worker task");
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
		
		plugin.getPearlManager().decayPearls();
	}
}
