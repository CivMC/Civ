package vg.civcraft.mc.civmodcore;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.chatDialog.ChatListener;
import vg.civcraft.mc.civmodcore.chatDialog.DialogManager;
import vg.civcraft.mc.civmodcore.command.CommandHandler;
import vg.civcraft.mc.civmodcore.command.StandaloneCommandHandler;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventoryListener;
import vg.civcraft.mc.civmodcore.itemHandling.NiceNames;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.scoreboard.ScoreBoardListener;

public abstract class ACivMod extends JavaPlugin {

	@Deprecated
	protected CommandHandler handle;

	protected StandaloneCommandHandler newCommandHandler;

	private static boolean initializedAPIs = false;
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (handle == null) {
			return newCommandHandler.executeCommand(sender, command, args);
		} else {
			return handle.execute(sender, command, args);
		}
	}

	@Override
	public void onEnable() {
		initApis(this);
		this.newCommandHandler = new StandaloneCommandHandler(this);
	}
	
	@Override
	public void onDisable() {
		PlayerSettingAPI.saveAll();
	}

	private static synchronized void initApis(ACivMod instance) {
		if (!initializedAPIs) {
			initializedAPIs = true;
			instance.registerEvents();
			new NiceNames().loadNames();
			new DialogManager();
			ConfigurationSerialization.registerClass(ManagedDatasource.class);
		}
	}

	private void registerEvents() {
		getServer().getPluginManager().registerEvents(new ClickableInventoryListener(), this);
		getServer().getPluginManager().registerEvents(new ChatListener(), this);
		getServer().getPluginManager().registerEvents(new ScoreBoardListener(), this);
	}

	@Deprecated
	public boolean toBool(String value) {
		if (value.equals("1") || value.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (handle == null) {
			return newCommandHandler.tabCompleteCommand(sender, cmd, args);
		} else {
			return handle.complete(sender, cmd, args);
		}
	}

	public CommandHandler getCommandHandler() {
		return handle;
	}

	protected void setCommandHandler(CommandHandler handle) {
		this.handle = handle;
	}

	protected abstract String getPluginName();

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
	 * Live activatable debug message (using {@link Config#DebugLog} to decide) at
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
	 * Live activatable debug message (using {@link Config#DebugLog} to decide) at
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
