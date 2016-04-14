package com.programmerdan.minecraft.simpleadminhacks;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.ArrayList;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Wrapper for simple admin hacks, each doing a thing and each configurable.
 * 
 * @author ProgrammerDan
 */
public class SimpleAdminHacks extends JavaPlugin {
	private static SimpleAdminHacks plugin;
	private SimpleAdminHacksConfig config;
	private ArrayList<SimpleHack> hacks;

	/**
	 * No-op constructor
	 */
	public SimpleAdminHacks() {
	}

	/**
	 * Deals with basic config processing, sets up global config and fires off
	 *  creation and registration of all hacks.
	 */
	public void onEnable() {
		SimpleAdminHacks.plugin = this;
		this.hacks = new ArrayList<SimpleHack>();
		
		// Config bootstrap
		this.saveDefaultConfig();
		this.reloadConfig();
		FileConfiguration conf = this.getConfig();
		try {
			this.config = new SimpleAdminHacksConfig(this, conf);
		} catch(InvalidConfigException e) {
			this.log(Level.SEVERE, "Failed to load config. Disabling plugin.", e);
			this.setEnabled(false);
			return;
		}

		// Warning if no hacks.
		if (hacks == null || hacks.size() == 0) {
			this.log("No hacks enabled.");
			return;
		}

		// Boot up the hacks.
		for (SimpleHack hack : hacks) {
			hack.enable();
		}

		this.getCommand("hacks").setExecutor(new CommandListener());
	}

	/**
	 * Forwards disable to hacks, clears instance and static variables
	 */
	public void onDisable() {
		if (hacks == null) return;
		for (SimpleHack hack : hacks) {
			hack.disable();
		}
		hacks.clear();
		hacks = null;
		config = null;
		SimpleAdminHacks.plugin = null;
	}

	/**
	 * Registers a new SimpleHack.
	 */
	public void register(SimpleHack hack) {
		if (hacks != null) {
			hacks.add(hack);
		}
	}

	/**
	 * Unregisters an existing SimpleHack.
	 */
	public void unregister(SimpleHack hack) {
		if (hacks != null) {
			hacks.remove(hack);
		}
	}

	/**
	 * Returns the psuedo-singleton instance of this plugin.
	 */
	public static SimpleAdminHacks instance() {
		return plugin;
	}

	/**
	 * Returns the overall config management for this plugin.
	 */
	public SimpleAdminHacksConfig config() {
		return config;
	}

	// ===== debug / logging methods =====

	private static final String debugPrefix = " [DEBUG] ";

	public void debug(String message) {
		if (!config.isDebug()) return;
		log(Level.INFO, SimpleAdminHacks.debugPrefix + message);
	}
	
	public void debug(String message, Object object) {
		if (!config.isDebug()) return;
		log(Level.INFO, SimpleAdminHacks.debugPrefix + message, object);
	}

	public void debug(String message, Throwable thrown) {
		if (!config.isDebug()) return;
		log(Level.INFO, SimpleAdminHacks.debugPrefix + message, thrown);
	}

	public void debug(String message, Object...objects) {
		if (!config.isDebug()) return;
		log(Level.INFO, SimpleAdminHacks.debugPrefix + message, objects);
	}
	
	public void log(String message) {
		getLogger().log(Level.INFO, message);
	}

	public void log(Level level, String message) {
		getLogger().log(level, message);
	}

	public void log(Level level, String message, Throwable thrown) {
		getLogger().log(level, message, thrown);
	}

	public void log(Level level, String message, Object object) {
		getLogger().log(level, message, object);
	}

	public void log(Level level, String message, Object...objects) {
		getLogger().log(level, message, objects);
	}
}
