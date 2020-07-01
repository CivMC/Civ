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
