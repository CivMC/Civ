package com.programmerdan.minecraft.civspy;

import com.programmerdan.minecraft.civspy.database.Database;

/**
 * Ultra-lightweight sampler for Bungee that simply records how many players are on the network, periodically.
 *
 * Generates: <code>bungee.playercount</code> stat_key data.
 *
 * @author ProgrammerDan
 */
public class CivSpyPlayerCount implements Runnable {
	private Database db;
	private CivSpyBungee plugin;

	public CivSpyPlayerCount(CivSpyBungee plugin, Database db) {
		this.plugin = plugin;
		this.db = db;
	}

	@Override
	public void run() {
		this.sample();
	}

	public void sample() {
		int playersNow = plugin.getProxy().getOnlineCount();

		this.db.insertData("bungee.playercount", playersNow);
	}
}
