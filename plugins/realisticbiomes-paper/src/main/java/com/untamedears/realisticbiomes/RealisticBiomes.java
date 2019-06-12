package com.untamedears.realisticbiomes;

import org.bukkit.plugin.PluginManager;

import com.untamedears.realisticbiomes.listener.AnimalListener;
import com.untamedears.realisticbiomes.listener.PlantListener;
import com.untamedears.realisticbiomes.listener.PlayerListener;
import com.untamedears.realisticbiomes.model.GlobalPlantManager;
import com.untamedears.realisticbiomes.model.RBDAO;

import vg.civcraft.mc.civmodcore.ACivMod;

public class RealisticBiomes extends ACivMod {

	private static RealisticBiomes plugin;

	public static RealisticBiomes getInstance() {
		return plugin;
	}

	private GrowthConfigManager growthConfigManager;
	private RBConfigManager configManager;
	private GlobalPlantManager plantManager;
	private AnimalConfigManager animalManager;
	private PlantLogicManager plantLogicManager;

	private RBDAO dao;

	public RBConfigManager getConfigManager() {
		return configManager;
	}

	public RBDAO getDAO() {
		return dao;
	}

	public GrowthConfigManager getGrowthConfigManager() {
		return growthConfigManager;
	}

	public PlantLogicManager getPlantLogicManager() {
		return plantLogicManager;
	}
	
	public GlobalPlantManager getPlantManager() {
		return plantManager;
	}

	@Override
	protected String getPluginName() {
		return "RealisticBiomes";
	}

	@Override
	public void onDisable() {
		if (plantManager != null) {
			plantManager.flushAll();
		}
	}

	@Override
	public void onEnable() {
		super.onEnable();
		RealisticBiomes.plugin = this;
		configManager = new RBConfigManager(this);
		if (!configManager.parse()) {
			return;
		}
		animalManager = new AnimalConfigManager();
		growthConfigManager = new GrowthConfigManager(configManager.getPlantGrowthConfigs());
		if (configManager.hasPersistentGrowthConfigs()) {
			this.dao = new RBDAO(configManager.getDatabase(), plugin);
			if (!dao.update()) {
				return;
			}
			plantManager = new GlobalPlantManager(dao);
			if (!plantManager.setup()) {
				return;
			}
		}
		plantLogicManager = new PlantLogicManager(plantManager, growthConfigManager);
		registerListeners();
	}

	private void registerListeners() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlantListener(this), this);
		pm.registerEvents(new AnimalListener(animalManager), this);
		pm.registerEvents(new PlayerListener(growthConfigManager, animalManager), this);
	}

}
