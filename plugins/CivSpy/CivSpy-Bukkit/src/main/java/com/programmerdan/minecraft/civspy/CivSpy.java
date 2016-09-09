package com.programmerdan.minecraft.civspy;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.HandlerList;

import com.programmerdan.minecraft.civspy.database.Database;

import com.google.common.reflect.ClassPath;
import java.lang.reflect.Constructor;


/**
 * CivSpy is an aggregation platform for vital tracking statistics.
 * It can also be used as a point data collector, with reduced effectiveness.
 * For holistic data collection, see Devotion: http://www.github.com/DevotedMC/Devotion
 * If you realize that the gigabytes of data that project will give you
 * are too much for your needs, come closer -- CivSpy might be what you
 * are looking for.
 *
 * @author ProgrammerDan
 */
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
			this.batcher = new DataBatcher(db, getLogger(), this.config.getMaxBatchSize(), 
					this.config.getMaxBatchWait(), this.config.getMaxBatchWorkers());

			getLogger().log(Level.INFO, "Preparing CivSpy Data Manager");
			this.manager = new DataManager(batcher, getLogger(), this.config.getAggregationPeriod(),
					this.config.getPeriodDelayCount(), this.config.getPeriodFutureCount(),
					this.config.getWorkerCount(), this.config.getFlowCaptureWindowCount(),
					this.config.getFlowCapturePeriod());
			
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
	
	private ArrayList<DataSampler> samplers;
	
	private ArrayList<DataListener> listeners;
	
	private void startSamplers() {
		getLogger().log(Level.INFO, "Registering CivSpy samplers");
		
		try {
			ClassPath getSamplersPath = ClassPath.from(this.getClassLoader());

			for (ClassPath.ClassInfo clsInfo : getSamplersPath.getTopLevelClasses("com.programmerdan.minecraft.civspy.samplers.impl")) {
				Class<?> clazz = clsInfo.load();
				getLogger().log(Level.INFO, "Found a class {0}, attempting to find a suitable constructor", clazz.getName());
				if (clazz != null && DataSampler.class.isAssignableFrom(clazz)) {
					DataSampler dataSampler = null;
					try {
						Constructor<?> constructBasic = clazz.getConstructor(DataManager.class, Logger.class, String.class);
						dataSampler = (DataSampler) constructBasic.newInstance(this.manager, this.getLogger(), this.config.getServer());
						getLogger().log(Level.INFO, "Created a new DataSampler of type {0}", clazz.getName());
					} catch (Exception e) {}

					if (dataSampler == null) {
						try {
							Constructor<?> constructBasic =
								clazz.getConstructor(DataManager.class, Logger.class, String.class, ConfigurationSection.class);
							dataSampler = (DataSampler) constructBasic.newInstance(this.manager, this.getLogger(), this.config.getServer(), 
								this.config.getSection(clazz));
							if (this.config.getSection(clazz) != null) {
								getLogger().log(Level.INFO, "Create a new DataSampler of type {0} with unique configuration", clazz.getName());
							} else {
								getLogger().log(Level.INFO, "Create a new DataSampler of type {0} with null configuration", clazz.getName());
							}
						} catch (Exception e) {}
					}

					if (dataSampler != null) {
						dataSampler.activate();
						// TODO: Use our own executor, and figure out how to bind to tick closest to period execution. 
						//    Perhaps wrapper task that registers a sync task for next tick?
						if (Bukkit.getScheduler().scheduleSyncRepeatingTask(this, dataSampler, 
							(long) (Math.random() * dataSampler.getPeriod())/50l, dataSampler.getPeriod()/50l) > -1) {
							samplers.add(dataSampler);
							getLogger().log(Level.INFO, "Class {0} scheduled as a DataSampler with period {1}.", 
									new Object[] {clazz.getName(), dataSampler.getPeriod()});
						} else {
							getLogger().log(Level.WARNING, "Class {0} failed to schedule as a DataSampler with period {1}.", 
									new Object[] {clazz.getName(), dataSampler.getPeriod()});
						}
					} else {
						getLogger().log(Level.INFO, "Class {0} is not suitable as a DataSampler.", clazz.getName());
					}
				}
			}
		} catch (IOException ioe) {
			getLogger().log(Level.WARNING, "Failed to load any samplers, due to IO error", ioe);
		}
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
		
		try {
			ClassPath getSamplersPath = ClassPath.from(this.getClassLoader());

			for (ClassPath.ClassInfo clsInfo : getSamplersPath.getTopLevelClasses("com.programmerdan.minecraft.civspy.listeners.impl")) {
				Class<?> clazz = clsInfo.load();
				getLogger().log(Level.INFO, "Found a class {0}, attempting to find a suitable constructor", clazz.getName());
				if (clazz != null && DataListener.class.isAssignableFrom(clazz)) {
					DataListener dataListener = null;
					try {
						Constructor<?> constructBasic = clazz.getConstructor(DataManager.class, Logger.class, String.class);
						dataListener = (DataListener) constructBasic.newInstance(this.manager, this.getLogger(), this.config.getServer());
						getLogger().log(Level.INFO, "Create a new DataListener of type {0}", clazz.getName());
					} catch (Exception e) {}

					if (dataListener == null) {
						try {
							Constructor<?> constructBasic =
								clazz.getConstructor(DataManager.class, Logger.class, String.class, ConfigurationSection.class);
							dataListener = (DataListener) constructBasic.newInstance(this.manager, this.getLogger(), this.config.getServer(), 
								this.config.getSection(clazz));
							if (this.config.getSection(clazz) != null) {
								getLogger().log(Level.INFO, "Create a new DataListener of type {0} with unique configuration", clazz.getName());
							} else {
								getLogger().log(Level.INFO, "Create a new DataListener of type {0} with null configuration", clazz.getName());
							}
						} catch (Exception e) {}
					}

					if (dataListener != null) {
						Bukkit.getPluginManager().registerEvents(dataListener, this);
						listeners.add(dataListener);
						getLogger().log(Level.INFO, "Class {0} registered as a DataListener.", clazz.getName());
					} else {
						getLogger().log(Level.INFO, "Class {0} is not suitable as a DataListener.", clazz.getName());
					}
				}
			}
		} catch (IOException ioe) {
			getLogger().log(Level.WARNING, "Failed to load any listeners, due to IO error", ioe);
		}
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
