package com.untamedears.jukealert.model.appender.config;

import java.util.concurrent.TimeUnit;
import org.bukkit.configuration.ConfigurationSection;
import vg.civcraft.mc.civmodcore.util.ConfigParsing;

public class DormantCullingConfig implements AppenderConfig {

	private long lifeTime;
	private long dormantLifeTime;

	public DormantCullingConfig(ConfigurationSection config) {
		lifeTime = ConfigParsing.parseTime(config.getString("lifeTime", "2 weeks"), TimeUnit.MILLISECONDS);
		dormantLifeTime = ConfigParsing.parseTime(config.getString("dormantTime", "2 weeks"), TimeUnit.MILLISECONDS);
	}

	/**
	 * @return How long should the snitch stay alive until it goes dormant, in milli
	 *         seconds
	 */
	public long getLifetime() {
		return lifeTime;
	}

	/**
	 * @return How long should the snitch stay dormant until it is removed entirely,
	 *         in milli seconds
	 */
	public long getDormantLifeTime() {
		return dormantLifeTime;
	}

	/**
	 * @return How long should the snitch stay alive from a refresh until it is
	 *         removed entirely, meaning the sum of dormant and normal life time, in
	 *         milli seconds
	 */
	public long getTotalLifeTime() {
		return dormantLifeTime + lifeTime;
	}

}
