package com.programmerdan.minecraft.civspy;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.nio.file.Files;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.event.EventHandler;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import com.programmerdan.minecraft.civspy.database.Database;

public class CivSpyBungee extends Plugin {

	private Configuration config;
	private Database db;
	private ScheduledTask tracker;
	private CivSpyPlayerCount counter;

	@Override
	public void onEnable() {
		getLogger().info("Getting CivSpyBungee configuration");
		this.config = loadConfig();

		if (this.config != null) {
			getLogger().info("Setting up Database for CivSpyBungee");
			this.db = configDatabase(config.getSection("database"));
		} else {
			getLogger().severe("Config not found, CivSpyBungee going dark.");
		}
		
		if (this.db != null && this.db.available()) {
			getLogger().info("Setting up CivSpyBungee Playercount Tracker");
			this.counter = new CivSpyPlayerCount(config, db);
			tracker = getProxy().getScheduler().schedule(this, this.counter,
					config.getInt("interval", 12000) * 50, config.getInt("interval", 12000) * 50, TimeUnit.SECONDS);
		} else {
			getLogger().severe("Database not connected, CivSpyBungee going dark.");
		}
	}

	@Override
	public void onDisable() {
		getLogger().info("Shutting down CivSpyBungee");
		getProxy().getScheduler().cancel(tracker);
		this.counter.sample();
		this.db.close();
	}


	private Configuration loadConfig() {
		if (!getDataFolder().exists())
			getDataFolder().mkdir();

		File file = new File(getDataFolder(), "config.yml");

		if (!file.exists()) {
			getLogger().info("Setting up CivSpyBungee default configuration");
			try (InputStream in = getResourceAsStream("config.yml")) {
				Files.copy(in, file.toPath());
			} catch (IOException e) {
				getLogger().log(Level.SEVERE, "Failed to save CivSpyBungee default config", e);
			}
		}

		try {
			return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
		} catch (IOException ioe) {
			getLogger().log(Level.SEVERE, "Failed to load CivSpyBungee config", ioe);
			return null;
		}
	}

	public DataBaseManager configDatabase(Configuration dbStuff) {
		if (dbStuff == null) {
			getLogger().severe("No database credentials specified. This plugin requires a database to run!");
			return null;
		}
		String host = dbStuff.getString("host");
		if (host == null) {
			getLogger().severe("No host for database specified. Could not load database credentials");
			return null;
		}
		int port = dbStuff.getInt("port", -1);
		if (port == -1) {
			getLogger().severe("No port for database specified. Could not load database credentials");
			return null;
		}
		String db = dbStuff.getString("database");
		if (db == null) {
			getLogger().severe("No name for database specified. Could not load database credentials");
			return null;
		}
		String user = dbStuff.getString("user");
		if (user == null) {
			getLogger().severe("No user for database specified. Could not load database credentials");
			return null;
		}
		String password = dbStuff.getString("password");
		if (password == null) {
			getLogger().warning("No password for database specified.");
		}
		return new Database(getLogger(), user, password, host, port, db);
	}

	@EventHandler
	public void afterLogin(PostLoginEvent event) {
		if (db == null) {
			getLogger().severe("Login occurred but no database configured. Skipping.");
			return;
		}

		ProxiedPlayer player = event.getPlayer();
		if (db.(player.getUniqueId())[0] == 0) {
			handleFirstLogin(player);
		} else {
			handleLogin(player);
		}
	}

	public void handleFirstLogin(ProxiedPlayer p) {
		dbm.initEssenceData(p.getUniqueId());
		handleLogin(p);
	}
	
	public void handleLogin(ProxiedPlayer p) {
		dbm.updateEssenceLogin(p.getUniqueId(), System.currentTimeMillis());
	}
}
