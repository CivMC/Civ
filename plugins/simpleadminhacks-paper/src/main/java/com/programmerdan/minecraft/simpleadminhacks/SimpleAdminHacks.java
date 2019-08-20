package com.programmerdan.minecraft.simpleadminhacks;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Wrapper for simple admin hacks, each doing a thing and each configurable.
 * 
 * @author ProgrammerDan
 */
public class SimpleAdminHacks extends JavaPlugin {
	
	private static SimpleAdminHacks plugin;
	private SimpleAdminHacksConfig config;
	private HackManager hackManager;

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

		// Config bootstrap
		this.saveDefaultConfig();
		this.reloadConfig();
		FileConfiguration conf = this.getConfig();
		try {
			this.config = new SimpleAdminHacksConfig(this, conf);
		} catch(InvalidConfigException e) {
			log(Level.SEVERE, "Failed to load config. Disabling plugin.", e);
			this.setEnabled(false);
			return;
		}
		this.hackManager = new HackManager(this);
		this.registerCommand("hacks", new CommandListener(this));
	}

	/**
	 * Forwards disable to hacks, clears instance and static variables
	 */
	@Override
	public void onDisable() {
		hackManager.disableAllHacks();
		hackManager = null;
		config = null;
		SimpleAdminHacks.plugin = null;
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

	private static final String debugPrefix = "[DEBUG] ";

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

	// Other non-static convenience aides

	public boolean serverHasPlugin(String pluginName) {
		return this.getServer().getPluginManager().isPluginEnabled(pluginName);
	}

	// Broadcast aides

	/**
	 * Broadcast to all online players having the plugin's default broadcast permission
	 */
	public int serverBroadcast(String message) {
		return this.getServer().broadcast(message, config().getBroadcastPermission());
	}

	/**
	 * Broadcast to all online players having the specified permission
	 */
	public int serverBroadcast(String message, String permission) {
		return this.getServer().broadcast(message, permission);
	}

	/**
	 * Broadcast to all online operators.
	 */
	public int serverOperatorBroadcast(String message) {
		int cnt = 0;
		for( OfflinePlayer op : this.serverOperators()) {
			if (op.isOnline() && op.getPlayer() != null) {
				op.getPlayer().sendMessage(message);
				cnt ++;
			}
		}
		return cnt;
	}

	/**
	 * Broadcast to all online players.
	 */
	public int serverOnlineBroadcast(String message) {
		int cnt = 0;
		for (Player p : this.serverOnlinePlayers()) {
			if ( p != null && p.isOnline() ) {
				p.sendMessage(message);
				cnt ++;
			}
		}
		return cnt;
	}

	/**
	 * Send a message to the Server Console
	 */
	public void serverSendConsoleMessage(String message) {
		this.serverConsoleSender().sendMessage(message);
	}

	// Non-static Server accessor helps (facilitates testing)

	public ConsoleCommandSender serverConsoleSender() {
		return this.getServer().getConsoleSender();
	}

	public Collection<? extends Player> serverOnlinePlayers() {
		return this.getServer().getOnlinePlayers();
	}

	public Set<OfflinePlayer> serverOperators(){
		return this.getServer().getOperators();
	}

	public void registerListener(Listener listener) {
		this.getServer().getPluginManager().registerEvents(listener, this);
	}

	public World serverGetWorld(String world) {
		World wurld = this.getServer().getWorld(world);
		if (world == null) {
			try {
				wurld = this.getServer().getWorld(UUID.fromString(world));
			} catch (IllegalArgumentException iae) {
				wurld = null; // not a UUID
			}
		}
		return wurld;
	}

	// Safe Registration Wrapping

	public void registerCommand(String command, CommandExecutor executor) {
		PluginCommand cmd = this.getCommand(command);
		if (cmd != null) {
			cmd.setExecutor(executor);
		} else {
			log(Level.WARNING, "Failed to register Executor for {0}, please define that command in the plugin.yml first.", command);
		}
	}

	/**
	 * Definitely not safe. Used for Insight hack.
	 * @return the ClassLoader for this plugin.
	 */
	public ClassLoader exposeClassLoader() {
		return this.getClassLoader();
	}
	
	/**
	 * @return Manager holding hack instances
	 */
	public HackManager getHackManager() {
		return hackManager;
	}

}
