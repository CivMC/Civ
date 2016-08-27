package vg.civcraft.mc.civmodcore;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * The sole purpose of this class is to make Spigot recognize this library as a plugin and automatically
 * load the classes onto the classpath for us.
 * <p>
 * Replaces Dummy class.
 */
public class CivModCorePlugin extends JavaPlugin {
	
	private static CivModCorePlugin instance;
	
	public void onEnable() {
		//needed for some of the apis
		instance = this;
	}

	/**
	 * Returns the logger for this plugin. Assumes the plugin has already been loaded.
	 *
	 * @return The logger for this plugin.
	 */
	public static Logger log() {
		return getInstance().getLogger();
	}
	
	public static CivModCorePlugin getInstance() {
		return instance;
	}

}
