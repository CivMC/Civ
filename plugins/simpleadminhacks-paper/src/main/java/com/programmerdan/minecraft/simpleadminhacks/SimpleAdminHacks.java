package com.programmerdan.minecraft.simpleadminhacks;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.reflect.ClassPath;

/**
 * Wrapper for simple admin hacks, each doing a thing and each configurable.
 * 
 * @author ProgrammerDan
 */
public class SimpleAdminHacks extends JavaPlugin {
	private static SimpleAdminHacks plugin;
	private SimpleAdminHacksConfig config;
	private List<SimpleHack<?>> hacks;

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
		this.hacks = new LinkedList<SimpleHack<?>>();

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


		// Now load all the Hacks and register.
		ConfigurationSection hackConfigs = this.getConfig().getConfigurationSection("hacks");
		try {
			ClassPath getSamplersPath = ClassPath.from(this.getClassLoader());

			for (ClassPath.ClassInfo clsInfo : getSamplersPath.getTopLevelClasses("com.programmerdan.minecraft.simpleadminhacks.hacks")) {
				try {
					Class<?> clazz = clsInfo.load();
					if (clazz != null && SimpleHack.class.isAssignableFrom(clazz)) {
						log(Level.INFO, "Found a hack class {0}, attempting to find a generating method and constructor", clazz.getName());
						ConfigurationSection hackConfig = hackConfigs.getConfigurationSection(clazz.getSimpleName());
						SimpleHackConfig hackingConfig = null;
						if (hackConfig != null) {
							try {
								Method genBasic = clazz.getMethod("generate", SimpleAdminHacks.class, ConfigurationSection.class);
								hackingConfig = (SimpleHackConfig) genBasic.invoke(null, this, hackConfig);
							} catch (IllegalAccessException failure) {
								log(Level.WARNING, "Creating configuration for hack {0} failed, illegal access failure", clazz.getName());
							} catch (IllegalArgumentException failure) {
								log(Level.WARNING, "Creating configuration for hack {0} failed, illegal argument failure", clazz.getName());
							} catch (InvocationTargetException failure) {
								log(Level.WARNING, "Creating configuration for hack {0} failed, invocation target failure", clazz.getName());
							}
						} else {
							log(Level.INFO, "Hack for {0} found but no configuration, skipping.", clazz.getSimpleName());
						}

						if (hackingConfig != null) {
							log(Level.INFO, "Configuration for Hack {0} found, instance: {1}", clazz.getSimpleName(), hackingConfig.toString());
							SimpleHack<?> hack = null;
							try {
								Constructor<?> constructBasic = clazz.getConstructor(SimpleAdminHacks.class, hackingConfig.getClass());
								hack = (SimpleHack<?>) constructBasic.newInstance(this, hackingConfig);
								log(Level.INFO, "Created a new Hack of type {0}", clazz.getSimpleName());
							} catch (InvalidConfigException ice) {
								log(Level.WARNING, "Failed to activate {0} hack, configuration failed", clazz.getSimpleName());
							} catch (Exception e) {
								log(Level.WARNING, "Failed to activate {0} hack, configuration failed: {1}", clazz.getSimpleName(), e.getMessage());
							}

							if (hack == null) {
								log(Level.WARNING, "Failed to create a Hack of type {0}", clazz.getSimpleName());
							} else {
								register(hack);
								log(Level.INFO, "Registered a new hack: {0}", clazz.getSimpleName());
							}
						} else {
							log(Level.INFO, "Configuration generation for Hack {0} failed, skipping.", clazz.getSimpleName());
						}
					}
				} catch (NoClassDefFoundError e) {
					log(Level.INFO, "Unable to load discovered class {0} due to dependency failure", clsInfo.getName());
				} catch (Exception e) {
					log(Level.WARNING, "Failed to complete hack discovery {0}", clsInfo.getName());
				}
			}
		} catch (Exception e) {
			log(Level.WARNING, "Failed to complete hack registration");
		}


		// Warning if no hacks.
		if (hacks == null || hacks.size() == 0) {
			log(Level.WARNING, "No hacks enabled.");
			return;
		}

		// Boot up the hacks.
		List<SimpleHack<?>> iterList = new ArrayList<SimpleHack<?>>(hacks);
		for (SimpleHack<?> hack : iterList) {
			try {
				hack.enable();
			} catch (NoClassDefFoundError err) {
				log(Level.WARNING, "Unable to activate hack {0}, missing dependency: {1}", hack.getName(), err.getMessage());
				unregister(hack);
			} catch (Exception e) {
				log(Level.WARNING, "Unable to activate hack {0}, unrecognized error: {1}", hack.getName(), e.getMessage());
				log(Level.WARNING, "Full stack trace: ", e);
				unregister(hack);
			}
		}

		this.registerCommand("hacks", new CommandListener(this));
	}

	/**
	 * Forwards disable to hacks, clears instance and static variables
	 */
	public void onDisable() {
		if (hacks == null) return;
		for (SimpleHack<?> hack : hacks) {
			try {
				hack.disable();
			} catch (NoClassDefFoundError err) {
				log(Level.WARNING, "Unable to cleanly disable hack {0}, missing dependency: {1}", hack.getName(), err.getMessage());
			} catch (Exception e) {
				log(Level.WARNING, "Unable to cleanly disable hack {0}, unrecognized error: {1}", hack.getName(), e.getMessage());
				log(Level.WARNING, "Full stack trace: ", e);
			}
		}
		hacks.clear();
		hacks = null;
		config = null;
		SimpleAdminHacks.plugin = null;
	}

	/**
	 * Registers a new SimpleHack.
	 */
	public void register(SimpleHack<?> hack) {
		if (hacks != null) {
			hacks.add(hack);
		}
	}

	/**
	 * Unregisters an existing SimpleHack.
	 */
	public void unregister(SimpleHack<?> hack) {
		if (hacks != null) {
			hacks.remove(hack);
		}
	}

	/**
	 * Returns a wrapped version of hacks preventing external removal but allowing interaction with the hacks.
	 */
	public List<SimpleHack<?>> getHacks() {
		return Collections.unmodifiableList(hacks);
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

}
