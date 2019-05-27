package com.untamedears.JukeAlert;

import org.bukkit.plugin.PluginManager;

import com.untamedears.JukeAlert.database.JukeAlertDAO;
import com.untamedears.JukeAlert.group.GroupMediator;
import com.untamedears.JukeAlert.listener.LoggableActionListener;
import com.untamedears.JukeAlert.listener.SnitchLifeCycleListener;
import com.untamedears.JukeAlert.manager.GlobalSnitchManager;
import com.untamedears.JukeAlert.manager.SnitchConfigManager;
import com.untamedears.JukeAlert.util.JukeAlertPermissionHandler;

import vg.civcraft.mc.civmodcore.ACivMod;

public class JukeAlert extends ACivMod {

	private static JukeAlert instance;

	public static JukeAlert getInstance() {
		return instance;
	}
	private JukeAlertDAO dao;
	private JAConfigManager configManager;
	private GlobalSnitchManager snitchManager;
	private GroupMediator groupMediator;
	private SnitchConfigManager snitchConfigManager;

	public JAConfigManager getConfigManager() {
		return configManager;
	}

	public GroupMediator getGroupMediator() {
		return groupMediator;
	}
	
	public SnitchConfigManager getSnitchConfigManager() {
		return snitchConfigManager;
	}

	public JukeAlertDAO getDAO() {
		return dao;
	}

	@Override
	protected String getPluginName() {		
		return "JukeAlert";
	}

	public GlobalSnitchManager getSnitchManager() {
		return snitchManager;
	}

	@Override
	public void onDisable() {
	}

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();
		configManager = new JAConfigManager(this);
		configManager.parse();
		snitchConfigManager = new SnitchConfigManager(configManager.getSnitchConfigs());
		groupMediator = new GroupMediator();
		snitchManager = new GlobalSnitchManager();
		registerJukeAlertEvents();
		JukeAlertPermissionHandler.setup();
	}

	private void registerJukeAlertEvents() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new LoggableActionListener(), this);
		pm.registerEvents(new SnitchLifeCycleListener(), this);
	}
}
