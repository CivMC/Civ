package com.programmerdan.minecraft.civspy;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.programmerdan.minecraft.civspy.database.Database;
import com.programmerdan.minecraft.civspy.listeners.BreakListener;
import com.programmerdan.minecraft.civspy.listeners.MovementListener;
import com.programmerdan.minecraft.civspy.samplers.PlayerCountSampler;
import com.programmerdan.minecraft.civspy.samplers.WorldPlayerCountSampler;


public class CivSpy extends JavaPlugin {

	private Config config;
	private Database db;
	
	private DataBatcher batcher;
	private DataManager manager;

	@Override
	public void onEnable() {
		getLogger().log(Level.INFO, "Initializing CivSpy config");
		this.config = new Config(getLogger()).setupConfig(this);

		getLogger().log(Level.INFO, "Initializing CivSpy database");
		this.db = config.parseDatabase();
		try {
			if (this.db == null){
				getLogger().log(Level.SEVERE, "Failed to acquire database, skipping managers, samplers, and listeners.");
				return;
			}
			this.db.available();
			
			getLogger().log(Level.INFO, "Preparing CivSpy Data Batcher");
			this.batcher = new DataBatcher(db, getLogger());

			getLogger().log(Level.INFO, "Preparing CivSpy Data Manager");
			this.manager = new DataManager(batcher, getLogger(), this.config.getAggregationPeriod(),
					this.config.getPeriodDelayCount(), this.config.getPeriodFutureCount());
			
			this.samplers = new ArrayList<DataSampler>();
			startSamplers();
			
			this.listeners = new ArrayList<DataListener>();
			startListeners();
			
		} catch (SQLException se) {
			getLogger().log(Level.SEVERE, "Failed to acquire database, skipping listeners", se);
		}
	}

	@Override
	public void onDisable() {
		getLogger().log(Level.INFO, "Deregistering CivSpy listeners");
		if (listeners != null) {
			stopListeners();
		}
		
		getLogger().log(Level.INFO, "Deregistering CivSpy samplers");
		if (samplers != null) {
			stopSamplers();
		}

		getLogger().log(Level.INFO, "Stopping CivSpy Data Manager");
		if (this.manager !=  null) {
			this.manager.shutdown();
		}

		getLogger().log(Level.INFO, "Stopping CivSpy Data Batcher");
		if (this.batcher != null) {
			this.batcher.shutdown();
		}
		
		getLogger().log(Level.INFO, "Closing CivSpy database");
		if (this.db != null) {
			try {
				this.db.close();
			} catch (SQLException se) {
				getLogger().log(Level.SEVERE, "Failed to close out CivSpy database");
			}
		}
	}
	
	ArrayList<DataSampler> samplers;
	
	ArrayList<DataListener> listeners;
	
	private void startSamplers() {
		getLogger().log(Level.INFO, "Registering CivSpy samplers");
		
		// SAMPLE
		getLogger().log(Level.INFO, "Registering server player count sampler");
		DataSampler pCount = new PlayerCountSampler(this.manager, this.getLogger(), this.config.getServer());
		pCount.activate();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, pCount, 1200l, 1200l);
		samplers.add(pCount);
		
		getLogger().log(Level.INFO, "Registering world player count sampler");
		DataSampler wCount = new WorldPlayerCountSampler(this.manager, this.getLogger(), this.config.getServer());
		wCount.activate();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, wCount, 1200l, 1200l);
		samplers.add(wCount);
		// END SAMPLE
	}
	
	/**
	 * Calls deactivate on each sampler (which internally halts sampling) then formally cancels
	 * the scheduled tasks.
	 */
	private void stopSamplers() {
		getLogger().log(Level.INFO, "Deregistering CivSpy samplers");
		
		// GENERIC deactivator
		for (DataSampler sampler : samplers) {
			sampler.deactivate();
		}
		
		// Turn them off.
		Bukkit.getScheduler().cancelTasks(this);
	}
	
	private void startListeners() {
		getLogger().log(Level.INFO, "Registering CivSpy listeners");
		
		// SAMPLE
		getLogger().log(Level.INFO, "Registering player movement listener");
		DataListener movement = new MovementListener(this.manager, this.getLogger(), this.config.getServer());
		Bukkit.getPluginManager().registerEvents(movement, this);
		listeners.add(movement);
		
		getLogger().log(Level.INFO, "Registering player block break listener");
		DataListener bbreak = new BreakListener(this.manager, this.getLogger(), this.config.getServer());
		Bukkit.getPluginManager().registerEvents(bbreak, this);
		listeners.add(bbreak);
		// END SAMPLE
	}
	
	/**
	 * Unregisters all listeners (so they stop firing) then calls shutdown on each.
	 */
	private void stopListeners() {
		getLogger().log(Level.INFO, "Deregistering CivSpy listeners");

		// Turn them off.
		HandlerList.unregisterAll((Plugin) this);
		
		// GENERIC cleanup.
		for (DataListener listener : listeners) {
			listener.shutdown();
		}
	}
}
