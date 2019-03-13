package com.untamedears.JukeAlert;

import java.util.LinkedList;
import java.util.logging.Level;

import org.bukkit.plugin.PluginManager;

import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import com.untamedears.JukeAlert.command.JukeAlertCommandHandler;
import com.untamedears.JukeAlert.command.commands.JaListCommand;
import com.untamedears.JukeAlert.group.GroupMediator;
import com.untamedears.JukeAlert.listener.ItemExchangeListener;
import com.untamedears.JukeAlert.listener.JukeAlertListener;
import com.untamedears.JukeAlert.listener.MercuryListener;
import com.untamedears.JukeAlert.manager.ConfigManager;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.storage.JukeAlertLogger;
import com.untamedears.JukeAlert.util.RateLimiter;

public class JukeAlert extends ACivMod {

	private static JukeAlert instance;

	private JukeAlertLogger jaLogger;

	private ConfigManager configManager;

	private SnitchManager snitchManager;

	private JukeAlertCommandHandler commandHandler;

	private GroupMediator groupMediator;

	private JaListCommand jaListCommand;

	@Override
	public void onEnable() {

		super.onEnable();
		instance = this;
		configManager = new ConfigManager();
		groupMediator = new GroupMediator();
		jaLogger = new JukeAlertLogger();
		snitchManager = new SnitchManager();
		jaListCommand = new JaListCommand();
		handle = new JukeAlertCommandHandler();
		handle.registerCommands();
		registerEvents();
		registerNameLayerPermissions();
		snitchManager.initialize();
		RateLimiter.initialize(this);
	}

	@Override
	public void onDisable() {

		snitchManager.saveSnitches();
	}

	private void registerEvents() {

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new JukeAlertListener(), this);
		if (pm.isPluginEnabled("ItemExchange")) {
			pm.registerEvents(new ItemExchangeListener(), this);
		}
		if (pm.isPluginEnabled("Mercury")) {
			pm.registerEvents(new MercuryListener(), this);
		}
	}

	@SuppressWarnings("unchecked")
	private void registerNameLayerPermissions() {

		LinkedList<PlayerType> memberAndAbove = new LinkedList<PlayerType>();
		LinkedList<PlayerType> modAndAbove = new LinkedList<PlayerType>();
		memberAndAbove.add(PlayerType.MEMBERS);
		memberAndAbove.add(PlayerType.MODS);
		memberAndAbove.add(PlayerType.ADMINS);
		memberAndAbove.add(PlayerType.OWNER);
		modAndAbove.add(PlayerType.MODS);
		modAndAbove.add(PlayerType.ADMINS);
		modAndAbove.add(PlayerType.OWNER);
		PermissionType.registerPermission("LIST_SNITCHES",
			(LinkedList<PlayerType>) modAndAbove.clone()); // Also tied to refreshing snitches
		PermissionType.registerPermission("SNITCH_NOTIFICATIONS", (LinkedList<PlayerType>) memberAndAbove.clone());
		PermissionType.registerPermission("READ_SNITCHLOG", (LinkedList<PlayerType>) memberAndAbove.clone());
		PermissionType.registerPermission("RENAME_SNITCH", (LinkedList<PlayerType>) modAndAbove.clone());
		PermissionType.registerPermission("SNITCH_IMMUNE", (LinkedList<PlayerType>) memberAndAbove.clone());
		PermissionType.registerPermission("LOOKUP_SNITCH", (LinkedList<PlayerType>) modAndAbove.clone());
		PermissionType.registerPermission("CLEAR_SNITCHLOG", (LinkedList<PlayerType>) modAndAbove.clone());
		PermissionType.registerPermission("SNITCH_TOGGLE_LEVER", (LinkedList<PlayerType>) modAndAbove.clone());
	}

	public static JukeAlert getInstance() {

		return instance;
	}

	public JukeAlertLogger getJaLogger() {

		return jaLogger;
	}

	public ConfigManager getConfigManager() {

		return configManager;
	}

	public SnitchManager getSnitchManager() {

		return snitchManager;
	}

	public GroupMediator getGroupMediator() {

		return groupMediator;
	}

	public JukeAlertCommandHandler getCommandHandler() {

		return commandHandler;
	}

	public JaListCommand getJaListCommand() {

		return jaListCommand;
	}

	// Logs a message with the level of Info
	public void log(String message) {

		this.getLogger().log(Level.INFO, message);
	}

	@Override
	protected String getPluginName() {		
		return "JukeAlert";
	}
}
