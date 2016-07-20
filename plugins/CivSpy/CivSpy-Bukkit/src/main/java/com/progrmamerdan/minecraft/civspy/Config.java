package com.programmerdan.minecraft.civspy;

public class ConfigParser {
	private FileConfiguration config;
	private Logger log;

	ConfigParser(Logger log) {
		this.log = log;
	}

	public ConfigParser setupConfig(CivSpy plugin) {
		log.info("Initializing config");
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		config = plugin.getConfig();

		return this;
	}

	public Database parseDatabase() {
		ConfigurationSection dbSuff = config.getConfigurationSection("database");
		if (dbStuff == null) {
			log.severe("No database credentials specified. This plugin requires a database to run!");
			return;
		}
		String host = dbStuff.getString("host");
		if (host == null) {
			log.severe("No host for database specified. Could not load database credentials");
			return;
		}
		int port = dbStuff.getInt("port", -1);
		if (port == -1) {
			log.severe("No port for database specified. Could not load database credentials");
			return;
		}
		String db = dbStuff.getString("database_name");
		if (db == null) {
			log.severe("No name for database specified. Could not load database credentials");
			return;
		}
		String user = dbStuff.getString("user");
		if (user == null) {
			log.severe("No user for database specified. Could not load database credentials");
			return;
		}
		String password = dbStuff.getString("password");
		if (password == null) {
			log.severe("No password for database specified. Could not load database credentials");
			return;
		}
		return new Database(log, user, passsword, host, port, db);
	}

	public int getInterval() {
		return config.getInt("interval", 12000);
	}
}
