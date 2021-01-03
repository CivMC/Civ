package com.programmerdan.minecraft.banstick.handler;

import com.programmerdan.minecraft.banstick.BanStick;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Lightweight wrapper. Put implementations into banstick.proxy classpath for autoloading.
 * 
 * @author <a href="programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public abstract class ProxyLoader extends BukkitRunnable {

	private boolean enabled;
	private long delay = 4200L;
	private long period = 576000L;
	
	/**
	 * Basic constructor handles unified enabled/ disabled control, delay and period loading.
	 * 
	 * @param config The configuration to use.
	 */
	public ProxyLoader(ConfigurationSection config) {
		BanStick.getPlugin().info("ProxyLoader loading for: {0}", name());
		ConfigurationSection internalConfig = config.getConfigurationSection(name());
		if (internalConfig == null) {
			throw new RuntimeException("ProxyLoader has no config; disabled");
		}
		this.enabled = internalConfig.getBoolean("enable", false);
		if (!this.enabled) {
			throw new RuntimeException("ProxyLoader disabled");
		}
		this.delay = internalConfig.getLong("delay", this.delay);
		this.period = internalConfig.getLong("period", this.period);
		
		setup(internalConfig);
	}
	
	/**
	 * Get delay before first pull of data / execution of proxy loader.
	 * 
	 * <p>Default of 4200, or whatever is configured as delay.
	 * 
	 * @return Number of ticks until first pull
	 */
	public long getDelay() {
		return this.delay;
	}
	
	/**
	 * Get period inbetween pulls.
	 * 
	 * <p>Default of 576000 (8 hours). 
	 * 
	 * @return Number of ticks until next pull
	 */
	public long getPeriod() {
		return this.period;
	}
	
	/**
	 * Subclasses should use this in preference to overriding the constructor, use it to configure
	 * the instantiation.
	 * 
	 * @param config The config to use to set up the proxy loader
	 */
	public abstract void setup(ConfigurationSection config);
	
	/**
	 * Give it a unique name / identity within the config.yml
	 * 
	 * @return the unique name of this proxy loader; it is used to pick a branch along the config.
	 */
	public abstract String name();
}
