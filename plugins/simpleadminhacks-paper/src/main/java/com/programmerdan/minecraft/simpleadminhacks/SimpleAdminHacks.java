package com.programmerdan.minecraft.simpleadminhacks;

import com.programmerdan.minecraft.simpleadminhacks.framework.HackManager;
import com.programmerdan.minecraft.simpleadminhacks.framework.commands.CommandRegistrar;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.ACivMod;

/**
 * Wrapper for simple admin hacks, each doing a thing and each configurable.
 *
 * @author ProgrammerDan
 */
public class SimpleAdminHacks extends ACivMod {

	private static SimpleAdminHacks plugin;

	private final SimpleAdminHacksConfig config;
	private final HackManager manager;
	private final CommandRegistrar commands;

	public SimpleAdminHacks() {
		plugin = this;
		this.config = new SimpleAdminHacksConfig(this);
		this.manager = new HackManager(this);
		this.commands = new CommandRegistrar(this);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		if (!this.config.parse()) {
			setEnabled(false);
			return;
		}
		this.manager.loadAllHacks();
	}

	@Override
	public void onDisable() {
		this.manager.disableAllHacks();
		this.commands.reset();
		this.config.reset();
		super.onDisable();
	}

	public void registerCommand(final String identifier, final CommandExecutor executor) {
		final PluginCommand command = getCommand(identifier);
		if (command != null) {
			command.setExecutor(executor);
		}
		else {
			warning("Failed to register Executor for " + identifier + ", " +
					"please define that command in the plugin.yml first.");
		}
	}

	/**
	 * Definitely not safe. Used for Insight hack.
	 * @return the ClassLoader for this plugin.
	 */
	public ClassLoader exposeClassLoader() {
		return getClassLoader();
	}

	/**
	 * @return Returns the overall config management for this plugin.
	 */
	public SimpleAdminHacksConfig config() {
		return this.config;
	}

	/**
	 * @return Returns the hack manager
	 */
	public HackManager getHackManager() {
		return this.manager;
	}

	/**
	 * @return Returns the command registrar
	 */
	public CommandRegistrar getCommands() {
		return this.commands;
	}

	/**
	 * @return Returns the pseudo-singleton instance of this plugin.
	 */
	public static SimpleAdminHacks instance() {
		return plugin;
	}

	// ------------------------------------------------------------
	// Deprecated
	// ------------------------------------------------------------

	@Deprecated
	public void log(String message) {
		info(message);
	}

	@Deprecated
	public void log(Level level, String message) {
		getLogger().log(level, message);
	}

	@Deprecated
	public void log(Level level, String message, Throwable thrown) {
		getLogger().log(level, message, thrown);
	}

	@Deprecated
	public void log(Level level, String message, Object object) {
		getLogger().log(level, message, object);
	}

	@Deprecated
	public void log(Level level, String message, Object...objects) {
		getLogger().log(level, message, objects);
	}

	@Deprecated
	public boolean serverHasPlugin(String pluginName) {
		return this.getServer().getPluginManager().isPluginEnabled(pluginName);
	}

	@Deprecated
	public int serverBroadcast(String message) {
		return this.getServer().broadcast(message, config().getBroadcastPermission());
	}

	@Deprecated
	public int serverOperatorBroadcast(String message) {
		int cnt = 0;
		for(OfflinePlayer op : Bukkit.getOperators()) {
			if (op.isOnline() && op.getPlayer() != null) {
				op.getPlayer().sendMessage(message);
				cnt ++;
			}
		}
		return cnt;
	}

	@Deprecated
	public int serverOnlineBroadcast(String message) {
		int cnt = 0;
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p != null && p.isOnline()) {
				p.sendMessage(message);
				cnt ++;
			}
		}
		return cnt;
	}

	@Deprecated
	public void serverSendConsoleMessage(String message) {
		Bukkit.getConsoleSender().sendMessage(message);
	}

}
