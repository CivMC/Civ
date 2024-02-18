package com.untamedears.realisticbiomes;

import com.untamedears.realisticbiomes.commands.RBCommandManager;
import com.untamedears.realisticbiomes.listener.AnimalListener;
import com.untamedears.realisticbiomes.listener.BonemealListener;
import com.untamedears.realisticbiomes.listener.MobListener;
import com.untamedears.realisticbiomes.listener.PlantListener;
import com.untamedears.realisticbiomes.listener.PlayerListener;
import com.untamedears.realisticbiomes.model.Plant;
import com.untamedears.realisticbiomes.model.RBChunkCache;
import com.untamedears.realisticbiomes.model.RBDAO;
import com.untamedears.realisticbiomes.replant.AutoReplantListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.BlockBasedChunkMetaView;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.ChunkMetaAPI;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableStorageEngine;

public class RealisticBiomes extends ACivMod {

	private static RealisticBiomes plugin;

	public static RealisticBiomes getInstance() {
		return plugin;
	}

	private GrowthConfigManager growthConfigManager;
	private RBConfigManager configManager;
	private PlantManager plantManager;
	private AnimalConfigManager animalManager;
	private PlantLogicManager plantLogicManager;
	private PlantProgressManager plantProgressManager;
	private RBCommandManager commandManager;

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

	public PlantManager getPlantManager() {
		return plantManager;
	}

	public PlantProgressManager getPlantProgressManager() {
		return plantProgressManager;
	}

	@Override
	public void onDisable() {
		dao.setBatchMode(true);
		if (plantManager != null) {
			plantManager.shutDown();
		}
		dao.cleanupBatches();
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
			this.dao = new RBDAO(getLogger(), configManager.getDatabase());
			if (!dao.updateDatabase()) {
				Bukkit.shutdown();
				return;
			}
			plantProgressManager = new PlantProgressManager();
			BlockBasedChunkMetaView<RBChunkCache, TableBasedDataObject, TableStorageEngine<Plant>> chunkMetaData = ChunkMetaAPI
					.registerBlockBasedPlugin(this, () -> {
						return new RBChunkCache(false, dao);
					}, dao, false);
			if (chunkMetaData == null) {
				getLogger().severe("Errors setting up chunk metadata API, shutting down");
				Bukkit.shutdown();
				return;
			}
			plantManager = new PlantManager(chunkMetaData);
		}
		plantLogicManager = new PlantLogicManager(plantManager, growthConfigManager);
		commandManager = new RBCommandManager(this);
		registerListeners();
	}

	private void registerListeners() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlantListener(this, plantManager, plantLogicManager), this);
		pm.registerEvents(new AnimalListener(animalManager), this);
		pm.registerEvents(new PlayerListener(growthConfigManager, animalManager, plantManager), this);
		pm.registerEvents(new BonemealListener(configManager.getBonemealPreventedBlocks()), this);
		pm.registerEvents(new MobListener(), this);
		if (getConfig().getBoolean("auto_replant_enabled", true)) {
			pm.registerEvents(new AutoReplantListener(getConfig().getBoolean("auto_replant_right_click", true)), this);
		}
	}

}
