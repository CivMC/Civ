package com.devotedmc.ExilePearl;

import java.util.Date;

import org.bukkit.Material;

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
	private Material feedMaterial = Material.COAL;
	private int feedAmount = 1;

	// 20 * 60 ticks/ minute
	private static final int TICKS_PER_MINUTE = 1200;
	private static final int MS_PER_MINUTE = 60000;

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
		long tickInterval = config.getResourceUpkeepIntervalMin() * TICKS_PER_MINUTE;
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


	@SuppressWarnings("deprecation")
	@Override
	public void run() {

		if (!enabled) {
			return;
		}

		Date lastFeed = plugin.getStorage().getLastFeedTime();
		Date now = new Date();

		// Make sure enough time has elapsed
		if (now.getTime() - lastFeed.getTime() < MS_PER_MINUTE) {
			plugin.log("Skipped pearl feeding - interval was too short.");
			return;
		}
		
		feedMaterial = Material.getMaterial(config.getResourceUpkeepMaterial());
		feedAmount = config.getResourceUpkeepAmount();

		// Update feed time
		plugin.getStorage().updateLastFeedTime(now);

		plugin.log("Feeding pearls.");

		// Iterate through all the pearls and reduce the strength
		// This will free any pearls that reach zero strength
		for (ExilePearl pearl : pearls.getPearls()) {
			if (pearl.verifyLocation()) {
				//feedPearl(pearl); TODO
			}
		}
	}
}
