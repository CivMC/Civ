package com.programmerdan.minecraft.civspy;

import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.civspy.database.Database;

public class Config {
	private FileConfiguration config;
	private Logger log;
	
	Config(Logger log) {
		this.log = log;
	}

	public Config setupConfig(CivSpy plugin) {
		log.info("Initializing config");
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		config = plugin.getConfig();

		return this;
	}

	public Database parseDatabase() {
		ConfigurationSection dbStuff = config.getConfigurationSection("database");
		if (dbStuff == null) {
			log.severe("No database credentials specified. This plugin requires a database to run!");
			return null;
		}
		String host = dbStuff.getString("host");
		if (host == null) {
			log.severe("No host for database specified. Could not load database credentials");
			return null;
		}
		int port = dbStuff.getInt("port", -1);
		if (port == -1) {
			log.severe("No port for database specified. Could not load database credentials");
			return null;
		}
		String db = dbStuff.getString("database");
		if (db == null) {
			log.severe("No name for database specified. Could not load database credentials");
			return null;
		}
		String user = dbStuff.getString("user");
		if (user == null) {
			log.severe("No user for database specified. Could not load database credentials");
			return null;
		}
		String password = dbStuff.getString("password");
		if (password == null) {
			log.severe("No password for database specified. Could not load database credentials");
			return null;
		}
		return new Database(log, user, password, host, port, db);
	}

	/**
	 * Which server are we running on? I could add BetterShards as a dependency, but for now just
	 * declare it.
	 * 
	 * @return "local" or whatever is defined as <code>server</code> in config.
	 */
	public String getServer() {
		return config.getString("server", "local");
	}
	
	/**
	 * How long to aggregate data into buckets before committing. A longer delay should not use
	 * much more memory then a shorter delay (due to aggregation effects) but does expose you to
	 * more data loss issues in case of shutdown. 
	 * @return Default of 60,000 milliseconds, or whatever is defined as <code>aggregation_period</code>
	 *   in config.
	 */
	public long getAggregationPeriod() {
		return config.getLong("aggregation_period", 60000l);
	}
	
	/**
	 * How many aggregation periods in the past do we hold on to, before we call it "done" and
	 * commit it to the database. A higher number ensures fewer stragglers are missed if we 
	 * run into situations where sampler inflow starts to exceed outflow and "older" data winds up
	 * coming off the queue.
	 * 
	 * Be careful with this; as this number increases the raw amount of data stored in memory
	 * continues to increase.
	 * 
	 * @return Default of 6, or whatever is defined in config as <code>period_delay_count</code>
	 */
	public int getPeriodDelayCount() {
		return config.getInt("period_delay_count", 6);
	}
	
	/**
	 * How many aggregation periods into the future do we pre-create? Recommend at least a few;
	 * this is to adjust for issues where the task to commit outgoing aggregation buckets falls behind, so
	 * that "current" (future from the perspective of the "stuck" queue) data isn't lost.
	 * This also speeds the actual transition from aggregation bucket to aggregation bucket.
	 * 
	 * @return Default of 3, or whatever is defined in config as <code>period_future_count</code>
	 */
	public int getPeriodFutureCount() {
		return config.getInt("period_future_count", 3);
	}


	/**
	 * Used by autoloader, grabs a configuration section with the same simple name as the class passed in, or null if nothing found.
	 *
	 * @param clazz A Class to find a configuration section with the same name
	 * @return null if not found, the configuration section otherwise.
	 */
	public ConfigurationSection getSection(Class clazz) {
		if (clazz == null) return null;
		return config.getConfigurationSection(clazz.getName());
	}
}
