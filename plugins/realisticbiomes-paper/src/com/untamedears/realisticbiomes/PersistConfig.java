package com.untamedears.realisticbiomes;

public class PersistConfig {
	public String databaseName;
	public boolean enabled;
	
	// the period in tick at which data from unloaded chunks will be loaded into
	// the database
	public long unloadBatchPeriod;
	
	// the chance that a grow_event on a block will trigger a plant chunk load
	public double growEventLoadChance;
	
	// flag that determines if the plugin should log db load and save events.
	public boolean logDB;
}