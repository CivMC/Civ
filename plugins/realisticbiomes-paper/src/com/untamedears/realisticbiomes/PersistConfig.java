package com.untamedears.realisticbiomes;

public class PersistConfig {
	
	// Database stuff
	public String databaseName;
	public String host;
	public String port;
	public String user;
	public String password;
	public String prefix;

	public boolean enabled;
	
	// the period in tick at which data from unloaded chunks will be loaded into
	// the database
	public long unloadBatchPeriod;
	
	// the maximum time in ms that may be spent unloading data, data no unloaded will be
	// unloaded at the next opportunity
	public long unloadBatchMaxTime;
	
	// the chance that a grow_event on a block will trigger a plant chunk load
	public double growEventLoadChance;
	
	// flag that determines if the plugin should log db load and save events.
	public boolean logDB;
}