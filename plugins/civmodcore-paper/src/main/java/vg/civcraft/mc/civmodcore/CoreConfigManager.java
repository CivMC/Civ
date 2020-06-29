package vg.civcraft.mc.civmodcore;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.util.Iteration;
import vg.civcraft.mc.civmodcore.util.TextUtil;

/**
 * This is a config parsing class intended to make handling configs a little easier, and automatically parse commonly
 * seen things within civ configs.
 */
public class CoreConfigManager {

	protected final ACivMod plugin;
	protected final Logger logger;

	private boolean debug;
	private boolean logReplies;

	public CoreConfigManager(ACivMod plugin) {
		this.plugin = plugin;
		this.logger = plugin.getLogger();
	}

	/**
	 * Parses this civ plugin's config. It will also save a default config (if one is not already present) and reload
	 * the config, so there's no need to do so yourself beforehand.
	 *
	 * @return Returns true if the config was successfully parsed.
	 */
	public final boolean parse() {
		this.plugin.info(ChatColor.BLUE + "Parsing config.");
		this.plugin.saveDefaultConfig();
		this.plugin.reloadConfig();
		FileConfiguration config = this.plugin.getConfig();
		// Parse debug value
		this.debug = config.getBoolean("debug", false);
		this.plugin.info("Debug mode: " + (this.debug ? "enabled" : "disabled"));
		// Parse reply logging value
		this.logReplies = config.getBoolean("logReplies", false);
		this.plugin.info("Logging replies: " + (this.logReplies ? "enabled" : "disabled"));
		// Allow child class parsing
		boolean worked = parseInternal(config);
		if (worked) {
			plugin.info(ChatColor.BLUE + "Config parsed.");
		}
		else {
			plugin.warning("Failed to parse config!");
		}
		return worked;
	}

	/**
	 * An internal parser method intended to be overriden by child classes.
	 *
	 * @param config The root config section.
	 * @return Return true if the
	 */
	protected boolean parseInternal(ConfigurationSection config) {
		return true;
	}

	/**
	 * This should reset all config values back to their defaults. Child classes should override this if they parse
	 * additional values that should be reset.
	 */
	public void reset() {
		this.debug = false;
		this.logReplies = false;
	}

	// ------------------------------------------------------------ //
	// Getters
	// ------------------------------------------------------------ //

	public final boolean isDebugEnabled() {
		return this.debug;
	}

	public final boolean logReplies() {
		return this.logReplies;
	}

	// ------------------------------------------------------------ //
	// Predefined parsing utilities
	// ------------------------------------------------------------ //

	/**
	 * Attempts to parse an integer from a config section, allowing for multiple keys to be attempted.
	 *
	 * @param config The config section to retrieve the integer from.
	 * @param fallback The value that should be falled back upon if a value cannot be found.
	 * @param keys The keys to attempt.
	 * @return Returns a retrieved value, or the fallback, which is allowed to be null.
	 */
	protected final Integer parseInteger(ConfigurationSection config, Integer fallback, String... keys) {
		if (config == null || Iteration.isNullOrEmpty(keys)) {
			return fallback;
		}
		for (String key : keys) {
			if (Strings.isNullOrEmpty(key)) {
				continue;
			}
			if (!config.isInt(key)) {
				continue;
			}
			return config.getInt(key);
		}
		return fallback;
	}

	/**
	 * Attempts to create a managed database connection with a config section. This is more robust than the default
	 * method of simply casting a get from the config.
	 *
	 * @param config The config section itself. {@code ManagedDatasource db = parseDatabase(config.get("database"));}
	 * @return Returns an instance of ManagedDatasource, or null if an error occurred while parsing.
	 */
	protected final ManagedDatasource parseDatabase(ConfigurationSection config) {
		if (config == null) {
			this.logger.warning("Cannot parse database.");
			return null;
		}
		// Parse username - required
		String username = config.getString("username", config.getString("user"));
		if (Strings.isNullOrEmpty(username)) {
			this.plugin.severe("Cannot parse database: username is missing or empty.");
			return null;
		}
		this.plugin.info("Database username: " + username);
		// Parse password - optional
		String password = config.getString("password", config.getString("pass", ""));
		this.plugin.info("Database password: " +
				(Strings.isNullOrEmpty(password) ? "<empty>" : TextUtil.repeat("*", password.length())));
		// Parse hostname - optional
		String hostname = config.getString("hostname", config.getString("host", "localhost"));
		this.plugin.info("Database hostname: " + hostname);
		// Parse port - optional
		Integer port = parseInteger(config, 3306, "port", "hostport");
		this.plugin.info("Database port: " + port);
		// Parse database - required
		String database = config.getString("database", config.getString("db"));
		if (Strings.isNullOrEmpty(database)) {
			this.plugin.severe("Cannot parse database: database is missing or empty.");
			return null;
		}
		this.plugin.info("Selected database: " + database);
		// Parse pool size - optional
		int poolSize = parseInteger(config, 5, "poolsize");
		this.plugin.info("Database maximum pool size: " + poolSize);
		// Parse timeout - optional
		int timeout = parseInteger(config, 10000, "connectionTimeout", "connection_timeout", "timeout");
		this.plugin.info("Database timeout: " + timeout);
		// Parse idle timeout - optional
		int idle = parseInteger(config, 600000, "idleTimeout", "idle_timeout", "idle");
		this.plugin.info("Database idle timeout: " + idle);
		// Parse max lifetime - optional
		int lifetime = parseInteger(config, 7200000, "maxLifetime", "max_lifetime", "lifetime");
		this.plugin.info("Database max lifetime: " + lifetime);
		// Attempt new database connection
		this.plugin.info("Database parsed.");
		return new ManagedDatasource(this.plugin, username, password, hostname, port, database,
				poolSize, timeout,idle, lifetime);
	}

	/**
	 * Attempts to retrieve a list of materials from a config section.
	 *
	 * @param config The config section.
	 * @param key The key of the list.
	 * @return Returns a list of materials, or null.
	 */
	protected final List<Material> parseMaterialList(ConfigurationSection config, String key) {
		return parseList(config, key, slug -> {
			Material found = MaterialAPI.getMaterial(slug);
			if (found == null) {
				this.logger.warning("Could not parse material \"" + slug + "\" at: " + config.getCurrentPath());
				return null;
			}
			return found;
		});
	}

	/**
	 * Attempts to retrieve a list from a config section.
	 *
	 * @param <T> The type to parse the list into.
	 * @param config The config section.
	 * @param key The key of the list.
	 * @param parser The parser to convert the string value into the correct type.
	 * @return Returns a list, or null.
	 */
	protected static <T> List<T> parseList(ConfigurationSection config, String key, Function<String, T> parser) {
		if (config == null || Strings.isNullOrEmpty(key) || !config.isList(key) || parser == null) {
			return null;
		}
		List<T> result = new ArrayList<>();
		for (String entry : config.getStringList(key)) {
			T item = parser.apply(entry);
			if (item != null) {
				result.add(item);
			}
		}
		return result;
	}

}
