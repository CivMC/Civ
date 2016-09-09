package com.programmerdan.minecraft.civspy.samplers.impl;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PeriodicDataSample;
import com.programmerdan.minecraft.civspy.samplers.ServerSingleDataSampler;

/**
 * A simple PlayerCountSampler that periodically records the server's player count.
 * 
 * @author ProgrammerDan
 */
public final class PlayerCountSampler extends ServerSingleDataSampler {
	
	public PlayerCountSampler(DataManager manager, Logger logger, String server, ConfigurationSection config) {
		super(manager, logger, server, config);
		if (config != null)
			this.setPeriod(config.getLong("period", this.getPeriod()));
	}

	/**
	 * Generates: <code>server.playercount</code> stat_key data.
	 */
	public DataSample sample() {
		if (getConfig() != null && getConfig().getBoolean("active", true) == false) {
			this.deactivate();
			return null;
		}
		DataSample ds = new PeriodicDataSample("server.playercount", this.getServer(),
				null, null, Bukkit.getOnlinePlayers().size());
		return ds;
	}

}
