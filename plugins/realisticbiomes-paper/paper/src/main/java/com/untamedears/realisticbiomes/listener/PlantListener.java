package com.untamedears.realisticbiomes.listener;

import com.untamedears.realisticbiomes.PlantLogicManager;
import com.untamedears.realisticbiomes.PlantManager;
import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.model.Plant;
import com.untamedears.realisticbiomes.model.PlantLoadState;
import com.untamedears.realisticbiomes.utils.RBUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.world.StructureGrowEvent;

public class PlantListener implements Listener {

	private final RealisticBiomes plugin;
	private PlantManager plantManager;
	private PlantLogicManager plantLogicManager;

	public PlantListener(RealisticBiomes plugin, PlantManager plantManager, PlantLogicManager plantLogicManager) {
		this.plugin = plugin;
		this.plantManager = plantManager;
		this.plantLogicManager = plantLogicManager;
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
		handleGrowEvent(event, event.getBlock(), event.getNewState().getType());
	}

	private void handleGrowEvent(Cancellable event, Block sourceBlock, Material material) {
		PlantLoadState state = plantManager.getPlantIfLoaded(RBUtils.getRealPlantBlock(sourceBlock));
		if (!state.isLoaded) {
			// RB cannot handle it now and therefore waiting for the next tick
			// but for now cancel it to prevent vanilla mechanics
			event.setCancelled(true);

			return;
		}

		Plant plant = state.plant;

		PlantGrowthConfig growthConfig;
		if (plant == null) {
			growthConfig = plugin.getGrowthConfigManager().getGrowthConfigFallback(material);
			if (growthConfig == null) {
				// vanilla
				return;
			}
			if (RBUtils.isFruit(material)) {
				growthConfig.handleAttemptedGrowth(event, sourceBlock);
				return;
			}
			if (growthConfig.isPersistent()) {
				growthConfig.handleAttemptedGrowth(event, sourceBlock);
				sourceBlock = plantLogicManager.remapColumnBlock(sourceBlock, growthConfig, material);
				plant = plantManager.getPlant(sourceBlock);
				if (plant == null) {
					// a plant should be here, but isn't
					plant = new Plant(sourceBlock.getLocation(), growthConfig);
					plantManager.putPlant(plant);
				}
			}
		} else {
			growthConfig = plant.getGrowthConfig();
			if (growthConfig == null) {
				growthConfig = plugin.getGrowthConfigManager().getGrowthConfigFallback(material);
			}
			if (growthConfig == null) {
				plantManager.deletePlant(plant);
				return;
			} else {
				plant.setGrowthConfig(growthConfig);
			}
		}
		if (growthConfig.isPersistent()) {
			plantLogicManager.updateGrowthTime(plant, sourceBlock);
		}
		growthConfig.handleAttemptedGrowth(event, sourceBlock);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		plugin.getPlantLogicManager().handlePlantCreation(event.getBlock(), event.getItemInHand());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onStructureGrow(StructureGrowEvent event) {
		// disable bonemeal
		if (event.isFromBonemeal()) {
			if (plugin.getConfigManager().getBonemealPreventedBlocks().contains(
					event.getLocation().getBlock().getType())) {
				event.setCancelled(true);
			}
		}
		// handle trees etc.
		Block block = event.getLocation().getBlock();
		handleGrowEvent(event, block, block.getType());
	}

	/*
	 * If Bamboo and Kelp stop answering to these events, this spigot bug might have
	 * been solved and should contain more info on how to update accordingly:
	 * https://hub.spigotmc.org/jira/browse/SPIGOT-5312
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockSpread(BlockSpreadEvent event) {
		handleGrowEvent(event, event.getSource(), event.getNewState().getType());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void cactusBreak(BlockPhysicsEvent event) {
		// - sourceBlock is affecting the other block
		//   If it is AIR then this means that the CACTUS block was replaced by AIR
		// - changedType is the block the was affected
		//   Must be CACTUS since we handling here CACTUS break event
		//   Growth time should be recalculated if the CACTUS was broken

		if (event.getChangedType() != Material.CACTUS
				|| event.getSourceBlock().getType() != Material.AIR
				|| event.getSourceBlock().getY() <= event.getBlock().getLocation().getY()
		) {
			return;
		}

		plugin.getPlantLogicManager().handleCactusPhysics(event.getBlock());
	}
}
