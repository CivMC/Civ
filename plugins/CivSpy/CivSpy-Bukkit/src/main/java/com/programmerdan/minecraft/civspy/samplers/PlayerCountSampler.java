package com.programmerdan.minecraft.civspy.samplers;

import java.util.logging.Logger;

import org.bukkit.Bukkit;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.SingleDataSampler;
import com.programmerdan.minecraft.civspy.PeriodicDataSample;

public final class PlayerCountSampler extends SingleDataSampler {
	
	private final String server;

	public PlayerCountSampler(DataManager manager, Logger logger, String server) {
		super(manager, logger);
		this.server = server;
	}

	/**
	 * Must be called by a Bukkit Synchronous task!
	 */
	public DataSample sample() {
		DataSample ds = new PeriodicDataSample("server.playercount", server,
				null, null, Bukkit.getOnlinePlayers().size());
		return ds;
	}

}
