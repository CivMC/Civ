package vg.civcraft.mc.civmodcore;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public abstract class CoreConfigManager {

	protected ACivMod plugin;
	
	private boolean debug;
	
	public CoreConfigManager(ACivMod plugin) {
		this.plugin = plugin;
	}
	
	public boolean parse() {
		plugin.info("Parsing config file of " + plugin.getName());
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();
		debug = config.getBoolean("debug", false);
		boolean worked = parseInternal(config);
		if (worked) {
			plugin.info("Successfully parsed config file of " + plugin.getName());
		}
		else {
			plugin.info("Failed to parse config file of " + plugin.getName() + ". Errors were encountered");
		}
		return worked;
	}
	
	public boolean isDebugEnabled() {
		return debug;
	}
	
	protected abstract boolean parseInternal(ConfigurationSection config);
}
