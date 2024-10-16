package com.programmerdan.minecraft.civspy;

import com.programmerdan.minecraft.civspy.database.Database;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

/**
 * Lightweight Bungee data sampler; at this point just tracks player sessions, so
 * has no complex infrastructure.
 * 
 * @author ProgrammerDan
 */
public class CivSpyBungee extends Plugin implements Listener {

	private Logger logger;
	private Configuration config;
	private Database db;
	private ScheduledTask tracker;
	private CivSpyPlayerCount counter;

	private HashMap<UUID, Long> players;

	@Override
	public void onEnable() {
		this.logger = getLogger();
		logger.info("Getting CivSpyBungee configuration");
		this.config = loadConfig();

		if (this.config != null) {
			logger.info("Setting up Database for CivSpyBungee");
			this.db = configDatabase(config.getSection("database"));
		} else {
			logger.severe("Config not found, CivSpyBungee going dark.");
		}
		
		try {
			if (this.db != null) {
				this.db.available();
				
				logger.info("Setting up CivSpyBungee Playercount Tracker");
				this.counter = new CivSpyPlayerCount(this, db);
				tracker = getProxy().getScheduler().schedule(this, this.counter,
						config.getInt("interval", 60000), config.getInt("interval", 60000), TimeUnit.MILLISECONDS);

				logger.info("Setting up CivSpyBungee Player Tracking");
				this.players = new HashMap<>();
				getProxy().getPluginManager().registerListener(this, this);
			} else {
				logger.severe("Database not connected, CivSpyBungee going dark.");
			}
		} catch(SQLException se) {
			logger.severe("Database failed connecting, CivSpyBungee going dark.");
		}
	}

	@Override
	public void onDisable() {
		logger.info("Shutting down CivSpyBungee");
		getProxy().getScheduler().cancel(tracker);
		this.counter.sample();
		for (UUID player : players.keySet()) {
			// TODO: Check that this doesn't double-insert.
			db.insertData("bungee.logout", player);
			session(player);
		}
		players.clear();
		try {
			this.db.close();
		} catch (SQLException se) {
			logger.log(Level.SEVERE, "Couldn't close out database and connections for CivSpyBungee");
		}
	}

	private Configuration loadConfig() {
		if (!getDataFolder().exists())
			getDataFolder().mkdir();

		File file = new File(getDataFolder(), "config.yml");

		if (!file.exists()) {
			logger.info("Setting up CivSpyBungee default configuration");
			try (InputStream in = getResourceAsStream("config.yml")) {
				Files.copy(in, file.toPath());
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Failed to save CivSpyBungee default config", e);
			}
		}

		try {
			return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
		} catch (IOException ioe) {
			logger.log(Level.SEVERE, "Failed to load CivSpyBungee config", ioe);
			return null;
		}
	}

	private Database configDatabase(Configuration dbStuff) {
		if (dbStuff == null) {
			logger.severe("No database credentials specified. This plugin requires a database to run!");
			return null;
		}
		String host = dbStuff.getString("host");
		if (host == null) {
			logger.severe("No host for database specified. Could not load database credentials");
			return null;
		}
		int port = dbStuff.getInt("port", -1);
		if (port == -1) {
			logger.severe("No port for database specified. Could not load database credentials");
			return null;
		}
		String db = dbStuff.getString("database");
		if (db == null) {
			logger.severe("No name for database specified. Could not load database credentials");
			return null;
		}
		String user = dbStuff.getString("user");
		if (user == null) {
			logger.severe("No user for database specified. Could not load database credentials");
			return null;
		}
		String password = dbStuff.getString("password");
		if (password == null) {
			logger.warning("No password for database specified.");
		}
		
		Integer poolSize = dbStuff.getInt("poolsize", 10);
		Long connectionTimeout = dbStuff.getLong("connectionTimeout", 10000L);
		Long idleTimeout = dbStuff.getLong("idleTimeout", 600_000L);
		Long maxLifetime = dbStuff.getLong("maxLifetime", 7_200_000L);
		
		return new Database(logger, user, password, host, port, db,
				poolSize, connectionTimeout, idleTimeout, maxLifetime);
	}

	/**
	 * Records player joining the network.
	 * 
	 * Generates: <code>bungee.login</code> stat_key data.
	 *
	 * @param event The login event
	 */
	@EventHandler
	public void afterLogin(PostLoginEvent event) {
		if (db == null) {
			return;
		}

		ProxiedPlayer player = event.getPlayer();
		db.insertData("bungee.login", player.getUniqueId());
		players.put(player.getUniqueId(), System.currentTimeMillis());
	}

	/**
	 * Records player departure from the network.
	 *
	 * Generates: <code>bungee.logout</code> stat_key data.
	 * 
	 * @param event The disconnect event
	 */
	@EventHandler
	public void afterLeave(PlayerDisconnectEvent event) {
		if (db == null) {
			return;
		}

		ProxiedPlayer player = event.getPlayer();
		UUID unid = player.getUniqueId();
		db.insertData("bungee.logout", unid);
		session(unid);
		players.remove(unid);
	}

	/**
	 * Computes the session length of the player in milliseconds.
	 *
	 * Generates: <code>bungee.session</code> stat_key data.
	 * 
	 * @param player The UUID of the player whose session length needs computing.
	 */
	public void session(UUID player) {
		Long start = players.get(player);
		if (start == null) {
			return;
		}
		db.insertData("bungee.session", player, System.currentTimeMillis() - start);
	}
}
