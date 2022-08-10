package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.model.Plant;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

public class TipGrower extends ColumnPlantGrower{

	private final Material tip;
	private final Material stem;
	private final BlockFace primaryGrowthDirection;
	private final int maxHeight;

	public TipGrower(Material tip, Material stem, BlockFace primaryGrowthDirection, int maxHeight) {
		super(maxHeight, tip, primaryGrowthDirection, false);
		this.tip = tip;
		this.stem = stem;
		this.primaryGrowthDirection = primaryGrowthDirection;
		this.maxHeight = maxHeight;
	}

	@Override
	public int getIncrementPerStage() {
		return 1;
	}

	@Override
	public int getMaxStage() {
		return this.maxHeight - 1;
	}

	@Override
	public int getStage(Plant plant) {
		Block plantBlock = plant.getLocation().getBlock();
		Block endBlock = plantBlock.getRelative(primaryGrowthDirection.getOppositeFace());
		int count = 0;
		for (int i = 0; i <= 384; i++) {
			endBlock = endBlock.getRelative(getPrimaryGrowthDirection());
			if (endBlock.getType() == getTipMaterial() || endBlock.getType() == getStemMaterial()) {
				count++;
				continue;
			}
			break;
		}
		int stage = count - 1;
		return Math.min(stage, getMaxStage());
	}

	@Override
	public boolean setStage(Plant plant, int stage) {
		int currentStage = getStage(plant);
		Block plantBlock = plant.getLocation().getBlock();
		if (stage <= currentStage) {
			return false;
		}
		return !growVertically(plant, getRelativeBlock(plantBlock), stage - currentStage).growthLimited();
	}

	@Override
	protected VerticalGrowResult growVertically(Plant plant, Block block, int howMany) {
		int counter = 0;
		Block onTop = block;
		BlockFace direction = null;
		if (block.getBlockData() instanceof Directional dir) {
			direction = dir.getFacing();
		}
		while (counter < getMaxStage() && howMany > 0) {
			counter++;
			onTop = onTop.getRelative(getPrimaryGrowthDirection());
			Material topMaterial = onTop.getType();
			if (topMaterial.isAir()) {
				onTop.setType(tip, true);
				if (direction != null) {
					Directional blockData = ((Directional) onTop.getBlockData());
					blockData.setFacing(direction);
					onTop.setBlockData(blockData);
				}
				howMany--;
				continue;
			}
			if (topMaterial == getTipMaterial() || topMaterial == getStemMaterial()) {
				// already existing block of the same plant
				continue;
			}
			// neither air, nor the right plant, but something else blocking growth, so we
			// stop
			break;
		}

		onTop = onTop.getType() != getTipMaterial() ? onTop.getRelative(getPrimaryGrowthDirection().getOppositeFace()) : onTop;

		return new VerticalGrowResult(howMany > 0, onTop);
	}

	@Override
	public boolean deleteOnFullGrowth() {
		return false;
	}

	//Gets the bottom block of the plant in which we store the plant object
	public Block getRelativeBlock(Block block) {
		Block placeholder = block;
		for (int i = 0; i < 384; i++) {
			Block relative = placeholder.getRelative(getPrimaryGrowthDirection());
			if (relative.getType() != getTipMaterial() || relative.getType() != getStemMaterial()) {
				break;
			}
			placeholder = relative;
		}
		return placeholder;
	}

	public Material getTipMaterial() {
		return this.tip;
	}

	public Material getStemMaterial() {
		return this.stem;
	}

	public BlockFace getPrimaryGrowthDirection() {
		return this.primaryGrowthDirection;
	}
}
