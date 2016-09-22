package com.devotedmc.ExilePearl;

import com.devotedmc.ExilePearl.util.Guard;

/**
 * Interval task that deducts strength from existing prison pearls
 * @author Gordon
 */
public class PearlWorker implements Runnable {

	private final ExilePearlPlugin plugin;
	private final PearlManager pearls;
	private final ExilePearlConfig config;

	private boolean enabled = false;
	private int taskId = 0;

	// 20 * 60 ticks/ minute
	private static final int TICKS_PER_MINUTE = 1200;

	/**
	 * Creates a new FactoryWorker instance
	 */
	public PearlWorker(final ExilePearlPlugin plugin, final PearlManager pearls, final ExilePearlConfig config) {
		Guard.ArgumentNotNull(plugin, "plugin");
		Guard.ArgumentNotNull(pearls, "pearls");
		Guard.ArgumentNotNull(config, "config");

		this.plugin = plugin;
		this.pearls = pearls;
		this.config = config;
	}


	/**
	 * Starts the worker task
	 */
	public void start() {
		long tickInterval = config.getPearlUpkeepIntervalMin() * TICKS_PER_MINUTE;
		taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 0, tickInterval);
		enabled = true;
	}

	/**
	 * Stops the worker task
	 */
	public void stop() {
		if (enabled) {
			plugin.getServer().getScheduler().cancelTask(taskId);
			enabled = false;
			taskId = 0;
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
	public void run() {

		if (!enabled) {
			return;
		}
		
		int feedAmount = config.getPearlUpkeepAmount();

		plugin.log("Feeding pearls.");

		// Iterate through all the pearls and reduce the strength
		// This will free any pearls that reach zero strength
		for (ExilePearl pearl : pearls.getPearls()) {
			if (pearl.verifyLocation()) {
				
				int udpatedStrength = pearl.getSealStrength() - feedAmount;
				if (udpatedStrength > 0) {
					pearl.setSealStrength(udpatedStrength);
				} else {
					plugin.log("Freeing pearl for player %s because the strength reached 0.", pearl.getPlayer().getName());
					pearls.freePearl(pearl);
				}
			}
		}
	}
}
