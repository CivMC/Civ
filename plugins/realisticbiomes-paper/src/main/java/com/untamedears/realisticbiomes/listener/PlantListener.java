package com.untamedears.realisticbiomes.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;

import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;

public class PlantListener implements Listener {

	private final RealisticBiomes plugin;

	public PlantListener(RealisticBiomes plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void on(BlockPistonExtendEvent event) {
		for (Block block : event.getBlocks()) {
			plugin.getPlantLogicManager().handleBlockDestruction(block);
		}
	}

	@EventHandler
	public void on(BlockPistonRetractEvent event) {
		plugin.getPlantLogicManager().handleBlockDestruction(event.getBlock());
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		plugin.getPlantLogicManager().handleBlockDestruction(event.getBlock());
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockGrow(BlockGrowEvent event) {
		Material material = event.getBlock().getType();
		PlantGrowthConfig growthConfig = plugin.getGrowthConfigManager().getPlantGrowthConfig(material);
		if (growthConfig != null) {
			growthConfig.handleAttemptedGrowth(event);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		plugin.getPlantLogicManager().handlePlantCreation(event.getBlock());
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		plugin.getPlantLogicManager().handleChunkLoad(e.getChunk());
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e) {
		plugin.getPlantLogicManager().handleChunkUnload(e.getChunk());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onStructureGrow(StructureGrowEvent event) {
		// disable bonemeal
		if (event.isFromBonemeal()) {
			event.setCancelled(true);
		}
	}
}
