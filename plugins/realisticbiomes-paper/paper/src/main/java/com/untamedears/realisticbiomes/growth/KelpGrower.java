package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.model.Plant;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class KelpGrower extends TipGrower{

	private Material tip = Material.KELP;
	private Material stem= Material.KELP_PLANT;
	private BlockFace primaryGrowthDirection = BlockFace.UP;
	private int maxHeight;

	public KelpGrower(int maxHeight) {
		super(Material.KELP, Material.KELP_PLANT, BlockFace.UP, maxHeight);
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
		return count - 1;
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
		while (counter < getMaxStage() && howMany > 0) {
			counter++;
			onTop = onTop.getRelative(getPrimaryGrowthDirection());
			Material topMaterial = onTop.getType();
			if (topMaterial == Material.WATER) {
				onTop.setType(getTipMaterial(), true);
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
		return super.deleteOnFullGrowth();
	}

	@Override
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

	@Override
	public Material getTipMaterial() {
		return Material.KELP;
	}

	@Override
	public Material getStemMaterial() {
		return Material.KELP_PLANT;
	}

	@Override
	public BlockFace getPrimaryGrowthDirection() {
		return BlockFace.UP;
	}
}
