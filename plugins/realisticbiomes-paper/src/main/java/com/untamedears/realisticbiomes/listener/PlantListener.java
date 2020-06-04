package com.untamedears.realisticbiomes.listener;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.world.StructureGrowEvent;

import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.model.Plant;

public class PlantListener implements Listener {

	private final RealisticBiomes plugin;

	public PlantListener(RealisticBiomes plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void on(BlockPistonExtendEvent event) {
		for (Block block : event.getBlocks()) {
			plugin.getPlantLogicManager().handleBlockDestruction(block);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void on(BlockPistonRetractEvent event) {
		plugin.getPlantLogicManager().handleBlockDestruction(event.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		plugin.getPlantLogicManager().handleBlockDestruction(event.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onExplosion(BlockExplodeEvent event) {
		plugin.getPlantLogicManager().handleBlockDestruction(event.getBlock());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockGrow(BlockGrowEvent event) {
		PlantGrowthConfig growthConfig = getGrowthConfigFallback(event.getBlock());
		if (growthConfig != null) {
			growthConfig.handleAttemptedGrowth(event, event.getBlock());
		}
	}
	
	private PlantGrowthConfig getGrowthConfigFallback(Block block) {
		Plant plant = plugin.getPlantManager().getPlant(block);
		PlantGrowthConfig growthConfig = null;
		if (plant != null) {
			growthConfig = plant.getGrowthConfig();
		}
		if (growthConfig == null) {
			growthConfig = plugin.getGrowthConfigManager().getGrowthConfigFallback(block.getType());
		}
		return growthConfig;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		plugin.getPlantLogicManager().handlePlantCreation(event.getBlock(), event.getItemInHand());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onStructureGrow(StructureGrowEvent event) {
		// disable bonemeal
		if (event.isFromBonemeal()) {
			event.setCancelled(true);
		}
		// handle trees etc.
		Plant plant = plugin.getPlantManager().getPlant(event.getLocation());
		if (plant != null) {
			plant.getGrowthConfig().handleAttemptedGrowth(event, event.getLocation().getBlock());
		}
	}

	/*
	 * If Bamboo and Kelp stop answering to these events, this spigot bug might have
	 * been solved and should contain more info on how to update accordingly:
	 * https://hub.spigotmc.org/jira/browse/SPIGOT-5312
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockSpread(BlockSpreadEvent event) {
		Plant plant = plugin.getPlantManager().getPlant(event.getSource());
		PlantGrowthConfig growthConfig = getGrowthConfigFallback(event.getBlock());
		if (growthConfig != null) {
			plant.getGrowthConfig().handleAttemptedGrowth(event, event.getSource());
		}
	}
}
