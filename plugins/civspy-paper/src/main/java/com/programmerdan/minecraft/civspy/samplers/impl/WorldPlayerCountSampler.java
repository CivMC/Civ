package com.programmerdan.minecraft.civspy.samplers.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PeriodicDataSample;
import com.programmerdan.minecraft.civspy.samplers.ServerMultiDataSampler;

public final class WorldPlayerCountSampler extends ServerMultiDataSampler {
	
	public WorldPlayerCountSampler(DataManager manager, Logger logger, String server, ConfigurationSection config) {
		super(manager, logger, server, config);
		if (config != null)
			this.setPeriod(config.getLong("period", this.getPeriod()));
	}

	/**
	 * Must be called by a Bukkit Synchronous task! Shows the player count in this world.
	 * 
	 * Generates: <code>world.playercount</code> stat_key data.
	 */
	public List<DataSample> sample() {
		if (getConfig() != null && getConfig().getBoolean("active", true) == false) {
			this.deactivate();
			return null;
		}
		List<World> worlds = Bukkit.getWorlds();
		List<DataSample> ds = new LinkedList<DataSample>();
		if (worlds != null) {
			for (World world : worlds) {
				ds.add(new PeriodicDataSample("world.playercount", this.getServer(),
						world.getName(), null, world.getPlayers().size()));
			}
			return ds;
		}
		return null;
	}

}
