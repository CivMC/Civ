package com.programmerdan.minecraft.civspy.samplers.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.civspy.CivSpy;
import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PeriodicDataSample;
import com.programmerdan.minecraft.civspy.samplers.ServerMultiDataSampler;

public final class TPSSampler extends ServerMultiDataSampler {
	private long currentMin = Long.MAX_VALUE;
	private long currentMax = Long.MIN_VALUE;
	private long sum = 0l;
	private long count = 0l;
	private long prior = 0l;
	
	private int tickTask = -1;
	
	private Object sync = new Object();
	
	public TPSSampler(DataManager manager, Logger logger, String server, ConfigurationSection config) {
		super(manager, logger, server, config);
		if (config != null) {
			this.setPeriod(config.getLong("period", this.getPeriod()));
		}
		prior = System.currentTimeMillis();
		
		tickTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(CivSpy.getPlugin(CivSpy.class),
				new Runnable() {
			@Override
			public void run(){
				tick();
			}
		}, 0l, 1l);
	}

	private void tick() {
		synchronized(sync){
			long next = System.currentTimeMillis();
			count++;
			long sofar = next - prior;
			if (sofar > currentMax) currentMax = sofar;
			if (sofar < currentMin) currentMin = sofar;
			sum += sofar;
			prior = next;
		}
	}
	/**
	 * Computes tick length average, min, max and stores it.
	 * 
	 * Generates: <code>server.tick.min</code> stat_key data.
	 * 			  <code>server.tick.max</code> stat_key data.
	 * 			  <code>server.tick.average</code> stat_key data.
	 */
	public List<DataSample> sample() {
		if (getConfig() != null && getConfig().getBoolean("active", true) == false) {
			Bukkit.getScheduler().cancelTask(tickTask);
			this.deactivate();
			return null;
		}
		synchronized(sync) {
			double average = (double) sum / (double) count;
			List<DataSample> ds = new LinkedList<DataSample>();
			ds.add(new PeriodicDataSample("server.tick.min", this.getServer(),
					null, null, currentMin));
			ds.add(new PeriodicDataSample("server.tick.max", this.getServer(),
					null, null, currentMax));
			ds.add(new PeriodicDataSample("server.tick.average", this.getServer(),
					null, null, average));
			currentMin = Long.MAX_VALUE;
			currentMax = Long.MIN_VALUE;
			sum = 0l;
			count = 0l;
			return ds;
		}
	}

}
