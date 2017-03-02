package com.programmerdan.minecraft.banstick.handler;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;

import com.programmerdan.minecraft.banstick.BanStick;

/**
 * Middleweight wrapper. Put implementations into banstick.scraper classpath for autoloading.
 * 
 * Handles error tracking, scheduling, and cooldown on error max.
 * 
 * 
 * @author ProgrammerDan
 *
 */
public abstract class ScraperWorker implements Runnable {

	private BukkitTask currentTask = null;
	private boolean enabled = false;
	private long delay = 4200l;
	private long period = 576000l;
	private long jitter = 176000l;
	private int maxErrors = 10;
	private long errorCooldown = -1l;
	
	private long lifetimeErrors = 0l;
	private int currentErrors = 0;
	
	/**
	 * Basic constructor handles unified enabled/ disabled control, delay and period and jitter loading.
	 * 
	 * Also pulls in max errors before shutdown and period until retry (if any)
	 * 
	 * @param config The configuration to use.
	 */
	public ScraperWorker(ConfigurationSection config) {
		BanStick.getPlugin().info("ScraperWorker loading for: {0}", name());
		ConfigurationSection internalConfig = config.getConfigurationSection(name());
		if (internalConfig == null) {
			throw new RuntimeException("ScraperWorker has no config; disabled");
		}
		this.enabled = internalConfig.getBoolean("enable", false);
		if (!this.enabled) {
			throw new RuntimeException("ScraperWorker disabled");
		}
		this.delay = internalConfig.getLong("delay", this.delay);
		this.period = internalConfig.getLong("period", this.period);
		this.jitter = internalConfig.getLong("jitter", this.jitter);
		this.maxErrors = internalConfig.getInt("maxErrors", this.maxErrors);
		this.errorCooldown = internalConfig.getLong("errorCooldown", this.errorCooldown);
		
		setup(internalConfig);
	}
	
	/**
	 * Get delay before first pull of data / execution of scraper worker.
	 * 
	 * Default of 4200, or whatever is configured as delay.
	 * 
	 * @return Number of ticks until first scrape
	 */
	public long getDelay() {
		return this.delay;
	}
	
	/**
	 * Get average period inbetween pulls.
	 * 
	 * Default of 576000 (8 hours). 
	 * 
	 * @return Number of ticks until next scrape, on average
	 */
	public long getPeriod() {
		return this.period;
	}
	
	/**
	 * Get +/- tick jitter inbetween executions, to disguise scrape	
	 * @return Number of ticks +/- to randomly adjust runtime of next scrape.
	 */
	public long getJitter() {
		return this.jitter;
	}
	
	/**
	 * Gets the max errors before entering cooldown / shutdown.
	 * 
	 * @return the max errors to tolerate
	 */
	public int getMaxErrors() {
		return this.maxErrors;
	}
	
	/**
	 * Returns the total errors encountered since this scraper began, ignore cooldown resets
	 * @return Total errors encountered
	 */
	public long getLifetimeErrors() {
		return this.lifetimeErrors;
	}
	
	/**
	 * Returns the errors encountered in this pre-cooldown segment.
	 * 
	 * @return Errors since last start / cooldown end.
	 */
	public int getCurrentErrors() {
		return this.currentErrors;
	}
	
	public void registerError() {
		this.currentErrors++;
		this.lifetimeErrors++;
	}
	
	/**
	 * Subclasses should use this in preference to overriding the constructor, use it to configure
	 * the instantiation.
	 * 
	 * @param config 
	 * The config to use to set up the scraper worker
	 */
	public abstract void setup(ConfigurationSection config);
	
	/**
	 * Give it a unique name / identity within the config.yml
	 * 
	 * @return the unique name of this scraper worker; it is used to pick a branch along
	 * the config.
	 */
	public abstract String name();
	
	/**
	 * Called on each run, should do the scraping
	 */
	public abstract void scrape();
	
	/**
	 * Takes the base and jitters it randomly by +/- the jitter amount. If this goes below zero,
	 * instead produces a random number between [0,jitter)
	 * @param base The base amount to jitter
	 * @return A jittered base
	 */
	private long jitter(long base) {
		long outcome = base + (long) (Math.random() * 2.0d * (double) this.jitter) - this.jitter;
		if (outcome < 1l) {
			outcome = (long) (Math.random() * (double) this.jitter);
		}
		return outcome;
	}
	
	/**
	 * Base run, handles uncaught exceptions and cooldown/ error counting.
	 */
	@Override
	public final void run() {
		try {
			BanStick.getPlugin().debug("Running Scraper Worker {0}", name());
			scrape();
		} catch (Exception e) {
			BanStick.getPlugin().warning("Scraper Worker {0} uncaught failure during scrape", name());
			BanStick.getPlugin().warning("Exception: ", e);
			registerError();
		}
		
		if (this.currentErrors > this.maxErrors) {
			// enter cooldown.
			this.currentErrors = 0;
			
			if (this.errorCooldown > 0) {
				BanStick.getPlugin().warning("Error threshold exceeded; cooldown engaged for {0}.", name());
				this.currentTask = Bukkit.getScheduler().runTaskLaterAsynchronously(BanStick.getPlugin(), this, jitter(this.errorCooldown));
			} else {
				this.enabled = false;
				BanStick.getPlugin().warning("Error threshold exceeded; {0} disabled.", name());
			}
		} else {
			this.currentTask = Bukkit.getScheduler().runTaskLaterAsynchronously(BanStick.getPlugin(), this, jitter(this.delay));
		}
	}
	
	public void shutdown() {
		if (this.currentTask != null) {
			this.currentTask.cancel();
		}
	}
	
	public void setTask(BukkitTask task) {
		this.currentTask = task;
	}
}