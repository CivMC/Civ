package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.model.Plant;
import com.untamedears.realisticbiomes.utils.RBUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public class VerticalGrower extends IArtificialGrower {
	public record VerticalGrowResult(boolean growthLimited, Block top){}
	
	public static Block getRelativeBlock(Block block, BlockFace face) {
		Material mat = block.getType();
		Block bottomBlock = block;
		// not actually using this variable, but just having it here as a fail safe
		for (int i = 0; i < 384; i++) {
			Block below = bottomBlock.getRelative(face);
			if (below.getType() != mat && below.getType() != RBUtils.getTipMaterial(mat) && below.getType() != RBUtils.getStemMaterial(mat)) {
				break;
			}
			bottomBlock = below;
		}
		return bottomBlock;
	}

	private int maxHeight;
	private Material material;
	private boolean instaBreakTouching;
	private BlockFace primaryGrowthDirection;
	
	public VerticalGrower(int maxHeight, Material material, BlockFace primaryGrowthDirection, boolean instaBreakTouching) {
		this.maxHeight = maxHeight;
		this.material = material;
		this.instaBreakTouching = instaBreakTouching;
		this.primaryGrowthDirection = primaryGrowthDirection;
	}
	
	public Material getMaterial() {
		return material;
	}

	@Override
	public int getIncrementPerStage() {
		return 1;
	}
	
	public BlockFace getPrimaryGrowthDirection() {
		return primaryGrowthDirection;
	}

	public boolean isInstaBreakTouching() {
		return instaBreakTouching;
	}

	@Override
	public int getMaxStage() {
		return maxHeight - 1;
	}

	@Override
	public int getStage(Plant plant) {
		Block block = plant.getLocation().getBlock();
		if (material != block.getType()) {
			return -1;
		}
		Block bottom = getRelativeBlock(block, primaryGrowthDirection.getOppositeFace());
		if (!bottom.getLocation().equals(block.getLocation())) {
			return -1;
		}
		int stage = getActualHeight(block) - 1;
		return Math.min(stage, getMaxStage());
	}

	protected int getActualHeight(Block block) {
		Block bottom = getRelativeBlock(block, BlockFace.DOWN);
		Block top = getRelativeBlock(block, BlockFace.UP);

		return top.getY() - bottom.getY() + 1;
	}

	/**
	 * Handles the growth of a column plant ( i.e sugarcane, cactus )
	 * 
	 * @param block   Block of the corresponding plant
	 * @param howMany How tall should the growth be
	 * @return highest plant block
	 */
	protected VerticalGrowResult growVertically(Plant plant, Block block, int howMany) {
		if (material != null && block.getType() != material) {
			block.setType(material);
		}

		int counter = 1;
		Block onTop = block;
		while (counter < maxHeight && howMany > 0) {
			counter++;
			onTop = onTop.getRelative(primaryGrowthDirection);
			Material topMaterial = onTop.getType();
			if (topMaterial == Material.AIR) {
				if (instaBreakTouching) {
					for(BlockFace face : WorldUtils.PLANAR_SIDES) {
						Block side = onTop.getRelative(face);
						if (!MaterialUtils.isAir(side.getType())) {
							ItemStack toDrop = plant.getGrowthConfig().getItem().clone();
							toDrop.setAmount(howMany);
							Location loc = block.getLocation();
							loc.add(0.5, 0.5, 0.5);
							Item item = block.getWorld().dropItemNaturally(loc, toDrop);
							item.setVelocity(item.getVelocity().multiply(1.2));
							plant.resetCreationTime();
							onTop = onTop.getRelative(primaryGrowthDirection.getOppositeFace());

							return new VerticalGrowResult(false, onTop);
						}
					}
				}
				onTop.setType(material, true);
				howMany--;
				continue;
			}
			if (topMaterial == block.getType()) {
				// already existing block of the same plant
				continue;
			}
			// neither air, nor the right plant, but something else blocking growth, so we
			// stop
			break;
		}

		onTop = onTop.getType() != material ? onTop.getRelative(primaryGrowthDirection.getOppositeFace()) : onTop;

		return new VerticalGrowResult(howMany > 0, onTop);
	}

	@Override
	public boolean setStage(Plant plant, int stage) {
		int currentStage = getStage(plant);
		if (stage <= currentStage) {
			return false;
		}
		Block block = plant.getLocation().getBlock();
		return !growVertically(plant, block, stage - currentStage).growthLimited;
	}

	@Override
	public boolean deleteOnFullGrowth() {
		return false;
	}
}
