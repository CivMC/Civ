package com.programmerdan.minecraft.civspy.samplers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PeriodicDataSample;

public final class WorldPlayerCountSampler extends ServerMultiDataSampler {
	
	public WorldPlayerCountSampler(DataManager manager, Logger logger, String server) {
		super(manager, logger, server);
	}

	/**
	 * Must be called by a Bukkit Synchronous task! Shows the player count in this world.
	 */
	public List<DataSample> sample() {
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
