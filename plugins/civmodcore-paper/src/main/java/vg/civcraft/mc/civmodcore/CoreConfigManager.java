package vg.civcraft.mc.civmodcore;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public abstract class CoreConfigManager {

	protected ACivMod plugin;
	protected Logger logger;

	private boolean debug;
	private boolean logReplies;

	public CoreConfigManager(ACivMod plugin) {
		this.plugin = plugin;
		this.logger = plugin.getLogger();
	}

	public boolean parse() {
		plugin.info("Parsing config file of " + plugin.getName());
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();
		debug = config.getBoolean("debug", false);
		logReplies = config.getBoolean("logReplies", false);
		boolean worked = parseInternal(config);
		if (worked) {
			plugin.info("Successfully parsed config file of " + plugin.getName());
		} else {
			plugin.info("Failed to parse config file of " + plugin.getName() + ". Errors were encountered");
		}
		return worked;
	}

	public boolean isDebugEnabled() {
		return debug;
	}

	public boolean logReplies() {
		return logReplies;
	}

	protected static <T> List<T> parseList(ConfigurationSection config, String key, Function<String, T> function) {
		if (config == null) {
			return null;
		}
		if (!config.isList(key)) {
			return null;
		}
		List<T> result = new LinkedList<>();
		for (String entry : config.getStringList(key)) {
			T item = function.apply(entry);
			if (item != null) {
				result.add(item);
			}
		}
		return result;
	}

	protected abstract boolean parseInternal(ConfigurationSection config);
}
