package com.untamedears.realisticbiomes;

import com.untamedears.realisticbiomes.growth.ColumnPlantGrower;
import com.untamedears.realisticbiomes.growth.FruitGrower;
import com.untamedears.realisticbiomes.growth.TipGrower;
import com.untamedears.realisticbiomes.growth.VerticalGrower;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.model.Plant;
import com.untamedears.realisticbiomes.utils.RBUtils;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public class PlantLogicManager {

	private PlantManager plantManager;
	private GrowthConfigManager growthConfigManager;
	private Set<Material> fruitBlocks;
	private Set<Material> columnBlocks;

	public PlantLogicManager(PlantManager plantManager, GrowthConfigManager growthConfigManager) {
		this.plantManager = plantManager;
		this.growthConfigManager = growthConfigManager;
		initAdjacentPlantBlocks(growthConfigManager.getAllGrowthConfigs());
	}

	public void handleBlockDestruction(Block block) {
		if (plantManager == null) {
			return;
		}
		Plant plant = plantManager.getPlant(block);
		if (plant != null) {
			plantManager.deletePlant(plant);
			return;
		}

		handleColumnBlockDestruction(block);
		handleFruitBlockDestruction(block);
	}

	public void handleColumnBlockDestruction(Block block) {
		// column plants will always hold the plant object in the bottom most block, so
		// we need
		// to update that if we just broke one of the upper blocks of a column plant

		if (columnBlocks == null || !columnBlocks.contains(block.getType()))
			return;

		Block sourceColumn = VerticalGrower.getRelativeBlock(block ,RBUtils.getGrowthDirection(block.getType()).getOppositeFace());
		Plant bottomColumnPlant = plantManager.getPlant(sourceColumn);
		if (bottomColumnPlant == null)
			return;

		if (bottomColumnPlant.getGrowthConfig() == null
				|| !(bottomColumnPlant.getGrowthConfig().getGrower() instanceof ColumnPlantGrower grower)) {
			// Fallback behaviour
			bottomColumnPlant.resetCreationTime();
		} else {
			Block topColumn = VerticalGrower.getRelativeBlock(block ,RBUtils.getGrowthDirection(block.getType()));
			int blocksBroken = Math.abs(topColumn.getY() - block.getY()) + 1;
			long growthTime = bottomColumnPlant.getGrowthConfig().getPersistentGrowthTime(sourceColumn, true);
			int stage = grower.getStage(bottomColumnPlant);
			if (stage == grower.getMaxStage()) {
				// If broken at max growth, set growth time offset from now based on amount of stages/blocks broken
				int stagesLeft = Math.max(stage - blocksBroken, 0);
				bottomColumnPlant.setCreationTime(System.currentTimeMillis() - (growthTime * stagesLeft) / stage);
			} else {
				// If not broken at max growth, increase creation time based on number of blocks broken
				long create = bottomColumnPlant.getCreationTime();
				bottomColumnPlant.setCreationTime(
						(long) Math.min(System.currentTimeMillis(), create + (growthTime * Math.min(1.0D, (blocksBroken / (double) grower.getMaxStage())))));
			}
		}

		updateGrowthTime(bottomColumnPlant, sourceColumn);
	}

	public void handleFruitBlockDestruction(Block block) {
		if (fruitBlocks == null || !fruitBlocks.contains(block.getType()))
			return;

		for(BlockFace face : WorldUtils.PLANAR_SIDES) {
			Block possibleStem = block.getRelative(face);
			Plant stem = plantManager.getPlant(possibleStem);
			if (stem == null) {
				continue;
			}
			if (stem.getGrowthConfig() == null || !(stem.getGrowthConfig().getGrower() instanceof FruitGrower)) {
				continue;
			}
			FruitGrower grower = (FruitGrower) stem.getGrowthConfig().getGrower();
			if (grower.getFruitMaterial() != block.getType()) {
				continue;
			}
			if (grower.getStage(stem) != grower.getMaxStage()) {
				continue;
			}
			if (grower.getTurnedDirection(possibleStem) == face.getOppositeFace()) {
				stem.resetCreationTime();
				grower.setStage(stem, 0);
				updateGrowthTime(stem, possibleStem);
			}
		}
	}

	private void initAdjacentPlantBlocks(Set<PlantGrowthConfig> growthConfigs) {
		for (PlantGrowthConfig config : growthConfigs) {
			if (config.getGrower() instanceof FruitGrower) {
				FruitGrower grower = (FruitGrower) config.getGrower();
				if (fruitBlocks == null) {
					fruitBlocks = new HashSet<>();
				}
				fruitBlocks.add(grower.getFruitMaterial());
				continue;
			}
			if (config.getGrower() instanceof ColumnPlantGrower) {
				ColumnPlantGrower grower = (ColumnPlantGrower) config.getGrower();
				if (columnBlocks == null) {
					columnBlocks = new HashSet<>();
				}
				columnBlocks.add(grower.getMaterial());
			}
			if (config.getGrower() instanceof TipGrower) {
				TipGrower grower = (TipGrower) config.getGrower();
				if (columnBlocks == null) {
					columnBlocks = new HashSet<>();
				}
				columnBlocks.add(grower.getTipMaterial());
				columnBlocks.add(grower.getStemMaterial());
			}
		}
	}

	public void handlePlantCreation(Block block, ItemStack itemUsed) {
		if (plantManager == null) {
			return;
		}
		PlantGrowthConfig growthConfig = growthConfigManager.getGrowthConfigByItem(itemUsed);
		if (growthConfig == null || !growthConfig.isPersistent()) {
			return;
		}
		if (growthConfig.getGrower() instanceof VerticalGrower verticalGrower) {
			BlockFace direction = verticalGrower.getPrimaryGrowthDirection().getOppositeFace();
			Material blockMaterial = block.getType();
			Material oppositeMaterial = block.getRelative(direction).getType();
			if (oppositeMaterial == blockMaterial
					|| oppositeMaterial == RBUtils.getStemMaterial(blockMaterial)
					|| oppositeMaterial == RBUtils.getTipMaterial(blockMaterial))
			{
				handleColumnBlockAdded(block);
				return;
			}
		}
		Plant plant = new Plant(block.getLocation(), growthConfig);
		Plant existingPlant = plantManager.getPlant(block);
		if (existingPlant != null) {
			plantManager.deletePlant(existingPlant);
		}
		plantManager.putPlant(plant);
		updateGrowthTime(plant, block);
	}

	private void handleColumnBlockAdded(Block block) {
		if (columnBlocks == null || !columnBlocks.contains(block.getType()))
			return;

		Block sourceColumn = VerticalGrower.getRelativeBlock(block ,RBUtils.getGrowthDirection(block.getType()).getOppositeFace());
		Plant bottomColumnPlant = plantManager.getPlant(sourceColumn);
		if (bottomColumnPlant == null)
			return;

		if (bottomColumnPlant.getGrowthConfig() == null
				|| !(bottomColumnPlant.getGrowthConfig().getGrower() instanceof ColumnPlantGrower grower))
		{
			return;
		}

		int stage = grower.getStage(bottomColumnPlant);
		if (stage == grower.getMaxStage())
			return;

		long create = bottomColumnPlant.getCreationTime();
		long growthTime = bottomColumnPlant.getGrowthConfig().getPersistentGrowthTime(sourceColumn, true);
		long newCreationTime = Math.min(System.currentTimeMillis(), create - growthTime / grower.getMaxStage());

		bottomColumnPlant.setCreationTime(newCreationTime);

		updateGrowthTime(bottomColumnPlant, sourceColumn);
	}
	
	public Block remapColumnBlock(Block block, PlantGrowthConfig growthConfig, Material material) {
		if (!columnBlocks.contains(block.getType())) {
			return block;
		}
		if (growthConfig.getGrower() instanceof VerticalGrower) {
			BlockFace direction = ((VerticalGrower) growthConfig.getGrower()).getPrimaryGrowthDirection();
			Block below = block.getRelative(direction);
			while (below.getType() == material) {
				below = below.getRelative(direction);
			}
			return below.getRelative(direction.getOppositeFace());
		}
		if (growthConfig.getGrower() instanceof TipGrower) {
			TipGrower config = ((TipGrower) growthConfig.getGrower());
			BlockFace direction = config.getPrimaryGrowthDirection();
			Block adjacent = block.getRelative(direction);
			while (adjacent.getType() == material || adjacent.getType() == config.getStemMaterial()) {
				adjacent = adjacent.getRelative(direction);
			}
			return adjacent.getRelative(direction.getOppositeFace());
		}
		return block;
	}

	public void updateGrowthTime(Plant plant, Block block) {
		PlantGrowthConfig growthConfig = plant.getGrowthConfig();
		if (growthConfig == null) {
			growthConfig = growthConfigManager.getGrowthConfigFallback(block.getType());
			if (growthConfig == null) {
				plantManager.deletePlant(plant);
				return;
			}
			plant.setGrowthConfig(growthConfig);
		}
		if (!growthConfig.isPersistent()) {
			return;
		}
		long nextUpdateTime = growthConfig.updatePlant(plant, block);
		if (nextUpdateTime != -1) {
			plant.setNextGrowthTime(nextUpdateTime);
		}
	}

}
