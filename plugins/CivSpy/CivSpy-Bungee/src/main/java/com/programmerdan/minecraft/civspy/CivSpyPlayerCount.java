package com.programmerdan.minecraft.civspy;

import com.programmerdan.minecraft.civspy.database.Database;

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
		int playersNow = plugin.getProxy().getPlayerCount();

		this.db.insertData("bungee.playercount", playersNow);
	}
}
