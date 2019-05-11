package com.untamedears.JukeAlert;

import java.util.LinkedList;

import org.bukkit.plugin.PluginManager;

import com.untamedears.JukeAlert.commands.JaListCommand;
import com.untamedears.JukeAlert.group.GroupMediator;
import com.untamedears.JukeAlert.listener.ItemExchangeListener;
import com.untamedears.JukeAlert.listener.JukeAlertListener;
import com.untamedears.JukeAlert.manager.ConfigManager;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.storage.JukeAlertLogger;
import com.untamedears.JukeAlert.util.RateLimiter;

import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class JukeAlert extends ACivMod {

	private static JukeAlert instance;

	public static JukeAlert getInstance() {
		return instance;
	}
	private JukeAlertLogger jaLogger;
	private ConfigManager configManager;
	private SnitchManager snitchManager;
	private GroupMediator groupMediator;

	private JaListCommand jaListCommand;

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public GroupMediator getGroupMediator() {
		return groupMediator;
	}

	public JaListCommand getJaListCommand() {
		return jaListCommand;
	}

	public JukeAlertLogger getJaLogger() {
		return jaLogger;
	}

	@Override
	protected String getPluginName() {		
		return "JukeAlert";
	}

	public SnitchManager getSnitchManager() {
		return snitchManager;
	}

	@Override
	public void onDisable() {
		snitchManager.saveSnitches();
	}

	@Override
	public void onEnable() {

		super.onEnable();
		instance = this;
		configManager = new ConfigManager();
		groupMediator = new GroupMediator();
		jaLogger = new JukeAlertLogger();
		snitchManager = new SnitchManager();
		jaListCommand = new JaListCommand();
		registerJukeAlertEvents();
		registerNameLayerPermissions();
		snitchManager.initialize();
		RateLimiter.initialize(this);
	}

	private void registerJukeAlertEvents() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new JukeAlertListener(), this);
		if (pm.isPluginEnabled("ItemExchange")) {
			pm.registerEvents(new ItemExchangeListener(), this);
		}
	}

	@SuppressWarnings("unchecked")
	private void registerNameLayerPermissions() {

		LinkedList<PlayerType> memberAndAbove = new LinkedList<>();
		LinkedList<PlayerType> modAndAbove = new LinkedList<>();
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
}
