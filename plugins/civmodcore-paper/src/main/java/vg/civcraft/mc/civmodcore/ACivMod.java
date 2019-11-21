package vg.civcraft.mc.civmodcore;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.command.CommandHandler;
import vg.civcraft.mc.civmodcore.command.StandaloneCommandHandler;

public abstract class ACivMod extends JavaPlugin {

	@Deprecated
	protected CommandHandler handle = null;

	protected StandaloneCommandHandler newCommandHandler;

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (handle == null) {
			return this.newCommandHandler.executeCommand(sender, command, args);
		} else {
			return this.handle.execute(sender, command, args);
		}
	}

	@Override
	public void onEnable() {
		this.newCommandHandler = new StandaloneCommandHandler(this);
	}
	
	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		Bukkit.getScheduler().cancelTasks(this);
	}

	protected void registerListener(Listener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("Cannot register a listener if it's null, you dummy");
		}
		getServer().getPluginManager().registerEvents(listener, this);
	}

	protected boolean isPluginEnabled(Plugin plugin) {
		if (plugin == null) {
			return false;
		}
		return getServer().getPluginManager().isPluginEnabled(plugin);
	}

	public void saveDefaultResource(String path) {
		if (getResource(path) == null) {
			saveResource(path, false);
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (this.handle == null) {
			return this.newCommandHandler.tabCompleteCommand(sender, cmd, args);
		} else {
			return this.handle.complete(sender, cmd, args);
		}
	}

	public CommandHandler getCommandHandler() {
		return this.handle;
	}

	protected void setCommandHandler(CommandHandler handle) {
		this.handle = handle;
	}

	/**
	 * Simple SEVERE level logging.
	 */
	public void severe(String message) {
		getLogger().log(Level.SEVERE, message);
	}

	/**
	 * Simple SEVERE level logging with Throwable record.
	 */
	public void severe(String message, Throwable error) {
		getLogger().log(Level.SEVERE, message, error);
	}

	/**
	 * Simple WARNING level logging.
	 */
	public void warning(String message) {
		getLogger().log(Level.WARNING, message);
	}

	/**
	 * Simple WARNING level logging with Throwable record.
	 */
	public void warning(String message, Throwable error) {
		getLogger().log(Level.WARNING, message, error);
	}

	/**
	 * Simple WARNING level logging with ellipsis notation shortcut for defered
	 * injection argument array.
	 */
	public void warning(String message, Object... vars) {
		getLogger().log(Level.WARNING, message, vars);
	}

	/**
	 * Simple INFO level logging
	 */
	public void info(String message) {
		getLogger().log(Level.INFO, message);
	}

	/**
	 * Simple INFO level logging with ellipsis notation shortcut for defered
	 * injection argument array.
	 */
	public void info(String message, Object... vars) {
		getLogger().log(Level.INFO, message, vars);
	}

	/**
	 * Live activatable debug message (using plugin's config.yml top level debug tag to decide) at
	 * INFO level.
	 *
	 * Skipped if DebugLog is false.
	 */
	public void debug(String message) {
		if (getConfig() != null && getConfig().getBoolean("debug", false)) {
			getLogger().log(Level.INFO, message);
		}
	}

	/**
	 * Live activatable debug message (using plugin's config.yml top level debug tag to decide) at
	 * INFO level with ellipsis notation shorcut for defered injection argument
	 * array.
	 *
	 * Skipped if DebugLog is false.
	 */
	public void debug(String message, Object... vars) {
		if (getConfig() != null && getConfig().getBoolean("debug", false)) {
			getLogger().log(Level.INFO, message, vars);
		}
	}

}
