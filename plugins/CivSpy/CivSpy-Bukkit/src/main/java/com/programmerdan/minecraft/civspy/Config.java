package com.programmerdan.minecraft.civspy;

import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.civspy.database.Database;

/**
 * Wraps the configuration file for Bukkit. "Knows how" to extract the 
 * parameters for the database. 
 * 
 * @author ProgrammerDan
 */
public class Config {
	private FileConfiguration config;
	private Logger log;
	
	Config(Logger log) {
		this.log = log;
	}

	public Config setupConfig(CivSpy plugin) {
		log.info("Initializing CivSpy config");
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
		Integer poolSize = dbStuff.getInt("poolsize", 10);
		Long connectionTimeout = dbStuff.getLong("connectionTimeout", 10000l);
		Long idleTimeout = dbStuff.getLong("idleTimeout", 600000l);
		Long maxLifetime = dbStuff.getLong("maxLifetime", 7200000l);
		
		return new Database(log, user, password, host, port, db,
				poolSize, connectionTimeout, idleTimeout, maxLifetime);
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
	 * @return Default of 60,000 milliseconds, or whatever is defined as <code>manager.aggregation_period</code>
	 *   in config.
	 */
	public long getAggregationPeriod() {
		return config.getLong("manager.aggregation_period", 60000l);
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
	 * @return Default of 6, or whatever is defined in config as <code>manager.period_delay_count</code>
	 */
	public int getPeriodDelayCount() {
		return config.getInt("manager.period_delay_count", 6);
	}
	
	/**
	 * How many aggregation periods into the future do we pre-create? Recommend at least a few;
	 * this is to adjust for issues where the task to commit outgoing aggregation buckets falls behind, so
	 * that "current" (future from the perspective of the "stuck" queue) data isn't lost.
	 * This also speeds the actual transition from aggregation bucket to aggregation bucket.
	 * 
	 * @return Default of 3, or whatever is defined in config as <code>manager.period_future_count</code>
	 */
	public int getPeriodFutureCount() {
		return config.getInt("manager.period_future_count", 3);
	}
	
	/**
	 * How many workers are taking data off the queue and pushing into aggregation bins? Due to some
	 * synchronization effects, don't pick too big a number; somewhere around 5 is probably more then enough.
	 * 
	 * @return Default of 5, or whatever is defined in config as <code>manager.worker_count</code>
	 */
	public int getWorkerCount() {
		return config.getInt("manager.worker_count", 5);
	}
	
	/**
	 * How many "windows" do we keep to monitor and normalize in flow vs. out flow of DataSamples? 
	 * Recommend 60.
	 * 
	 * @return Default of 60, or whatever is defined in config as <code>manager.flow_capture_window_count</code>
	 */
	public int getFlowCaptureWindowCount() {
		return config.getInt("manager.flow_capture_window_count", 60);
	}
	
	/**
	 * How big is each window we use to monitor and normalize in flow vs. out flow? Total monitoring / smoothing
	 * period is then window count * capture period. Typical to monitor about a minute's worth of inflow/outflow,
	 * but 10 minutes would be fine too; default total is 1 minute (60000 milliseconds).
	 * 
	 * @return Default of 1000, or whatever is defined in config as <code>manager.flow_capture_period</code>
	 */
	public long getFlowCapturePeriod() {
		return config.getLong("manager.flow_capture_period", 1000l);
	}

	/**
	 * What is the upper limit per-batch in terms of records? Recommended 100, which is default.
	 *
	 * @return Default of 100, or whatever is defined in config as <code>batcher.max_batch_size</code>
	 */
	public long getMaxBatchSize() {
		return config.getLong("batcher.max_batch_size", 100l);
	}

	/**
	 * What is the max time for a batch worker to sit around waiting for enough data to reach the max_batch_size?
	 * Recommended 1000l, which is default; Note: this assumes that connection timeout is longer then 1 second!
	 * If connection timeout is shorter then one second, <i>do not forget</i> to make this shorter then connection
	 * timeout.
	 *
	 * @return Default of 1000, or whatever is defined in config as <code>batcher.max_batch_wait</code>
	 */
	public long getMaxBatchWait() {
		return config.getLong("batcher.max_batch_wait", 1000l);
	}

	/**
	 * What is the most number of batch workers that can be employed? This should tie to # of connections available
	 * in the pool; don't allow more workers then you've allowed connections. Recommend # of workers less then pool size.
	 *
	 * @return Default of 5, or whatever is defined in config as <code>batcher.max_workers</code>
	 */
	public int getMaxBatchWorkers() {
		return config.getInt("batcher.max_workers", 5);
	}

	/**
	 * Used by autoloader, grabs a configuration section with the same simple name as the class passed in, or null if nothing found.
	 *
	 * @param clazz A Class to find a configuration section with the same name
	 * @return null if not found, the configuration section otherwise.
	 */
	public ConfigurationSection getSection(Class<?> clazz) {
		if (clazz == null) return null;
		return config.getConfigurationSection(clazz.getSimpleName());
	}
}
