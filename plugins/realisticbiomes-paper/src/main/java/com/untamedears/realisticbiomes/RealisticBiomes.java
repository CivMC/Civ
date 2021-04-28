package com.untamedears.realisticbiomes;

import com.untamedears.realisticbiomes.listener.AnimalListener;
import com.untamedears.realisticbiomes.listener.BonemealListener;
import com.untamedears.realisticbiomes.listener.PlantListener;
import com.untamedears.realisticbiomes.listener.PlayerListener;
import com.untamedears.realisticbiomes.model.Plant;
import com.untamedears.realisticbiomes.model.RBChunkCache;
import com.untamedears.realisticbiomes.model.RBDAO;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.api.BlockBasedChunkMetaView;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.api.ChunkMetaAPI;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableStorageEngine;

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
	private Queue<Runnable> rbTaskQueue;
	private BukkitTask taskQueueTask;

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
		taskQueueTask.cancel();
		getLogger().info("Running remaining tasks");
		while (!rbTaskQueue.isEmpty()) {
			try {
				rbTaskQueue.poll().run();
			} catch (Exception e) {
				getLogger().warning("Exception thrown by task while disabling plugin" + e);
			}
		}
		dao.setBatchMode(true);
		if (plantManager != null) {
			plantManager.shutDown();
		}
		dao.cleanupBatches();
	}

	@Override
	public void onEnable() {
		this.useNewCommandHandler = true;
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
		registerListeners();

		rbTaskQueue = new LinkedBlockingQueue<>();
		taskQueueTask = Bukkit.getScheduler().runTaskTimer(RealisticBiomes.getInstance(), () -> {
			long end = System.currentTimeMillis() + 20;
			while (!rbTaskQueue.isEmpty() && System.currentTimeMillis() < end) {
				rbTaskQueue.poll().run();
			}
		}, 1L, 1L);
	}

	private void registerListeners() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlantListener(this, plantManager, plantLogicManager), this);
		pm.registerEvents(new AnimalListener(animalManager), this);
		pm.registerEvents(new PlayerListener(growthConfigManager, animalManager, plantManager), this);
		pm.registerEvents(new BonemealListener(configManager.getBonemealPreventedBlocks()), this);
	}

	public void addTask(Runnable runnable) {
		rbTaskQueue.add(runnable);
	}
}
