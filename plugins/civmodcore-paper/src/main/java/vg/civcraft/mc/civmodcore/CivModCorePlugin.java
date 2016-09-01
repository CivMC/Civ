package vg.civcraft.mc.civmodcore;

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

	public static CivModCorePlugin getInstance() {
		return instance;
	}

}
