package vg.civcraft.mc.civmodcore.config;

import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * This is a config parsing class intended to make handling configs a little easier, and automatically parse commonly
 * seen things within civ configs.
 */
public abstract class ConfigParser {

	protected final Plugin plugin;
	protected final Logger logger;

	private boolean debug;
	private boolean logReplies;

	public ConfigParser(final Plugin plugin) {
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
		this.logger.info(ChatColor.BLUE + "Parsing config.");
		this.plugin.saveDefaultConfig();
		FileConfiguration config = this.plugin.getConfig();
		// Parse debug value
		this.debug = config.getBoolean("debug", false);
		this.logger.info("Debug mode: " + (this.debug ? "enabled" : "disabled"));
		// Parse reply logging value
		this.logReplies = config.getBoolean("logReplies", false);
		this.logger.info("Logging replies: " + (this.logReplies ? "enabled" : "disabled"));
		// Allow child class parsing
		final boolean worked = parseInternal(config);
		if (worked) {
			this.logger.info(ChatColor.BLUE + "Config parsed.");
		}
		else {
			this.logger.warning("Failed to parse config!");
		}
		return worked;
	}

	/**
	 * An internal parser method intended to be overridden by child classes.
	 *
	 * @param config The root config section.
	 * @return Return true if the
	 */
	protected abstract boolean parseInternal(final ConfigurationSection config);

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

}
