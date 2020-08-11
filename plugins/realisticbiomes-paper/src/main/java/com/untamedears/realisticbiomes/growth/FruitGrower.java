package com.untamedears.realisticbiomes.growth;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

import com.untamedears.realisticbiomes.model.Plant;
import com.untamedears.realisticbiomes.utils.RBUtils;

import vg.civcraft.mc.civmodcore.api.BlockAPI;

public class FruitGrower extends IArtificialGrower {

	private List<Material> validSoil = Arrays.asList(Material.DIRT, Material.GRASS_BLOCK, Material.FARMLAND);

	@Override
	public int getIncrementPerStage() {
		return 1;
	}

	@Override
	public int getMaxStage() {
		return 1;
	}

	@Override
	public int getStage(Plant plant) {
		Block block = plant.getLocation().getBlock();
		if (hasPlant(block)) {
			return 1;
		}
		return 0;
	}

	private void growFruit(Block block) {
		for (BlockFace face : BlockAPI.PLANAR_SIDES) {
			Block fruitBlock = block.getRelative(face);
			if (fruitBlock.getType() != Material.AIR) {
				continue;
			}
			Block below = fruitBlock.getRelative(BlockFace.DOWN);
			if (!validSoil.contains(below.getType())) {
				continue;
			}
			Material fruitMat = RBUtils.getFruit(block.getType());
			if (fruitMat != null) {
				// TODO attach stem?
				fruitBlock.setType(fruitMat, true);
				return;
			}
		}
	}

	private static boolean hasPlant(Block block) {
		BlockData data = block.getBlockData();
		if (data instanceof Directional) {
			Directional dir = (Directional) data;
			return block.getRelative(dir.getFacing()).getType() == RBUtils.getFruit(block.getType());
		}
		return false;
	}

	@Override
	public void setStage(Plant plant, int stage) {
		if (stage == 0) {
			// TODO We could remove the attached fruit if one exists, would that ever be
			// desired behavior?
			return;
		}
		Block block = plant.getLocation().getBlock();
		growFruit(block);
	}

}
