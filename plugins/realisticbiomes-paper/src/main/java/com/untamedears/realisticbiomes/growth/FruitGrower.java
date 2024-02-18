package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.model.Plant;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public class FruitGrower extends IArtificialGrower {

	private List<Material> validSoil = Arrays.asList(Material.DIRT, Material.GRASS_BLOCK, Material.FARMLAND);
	private Material attachedStem;
	private Material nonAttachedStem;
	private Material fruitMaterial;

	public FruitGrower(Material fruitMaterial, Material attachedStem, Material nonAttachedStem) {
		this.fruitMaterial = fruitMaterial;
		this.attachedStem = attachedStem;
		this.nonAttachedStem = nonAttachedStem;
	}
	
	public Material getAttachedStemMaterial() {
		return attachedStem;
	}
	
	public Material getNonattachedStemMaterial() {
		return nonAttachedStem;
	}
	
	public Material getFruitMaterial() {
		return fruitMaterial;
	}

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
		if (block.getType() == attachedStem) {
			if (hasPlant(block)) {
				return 1;
			}
			setStage(plant, 0); // doesn't have a fruit, but is attached type, so fix
			return 0;
		}
		if (block.getType() != nonAttachedStem) {
			return -1;
		}
		return 0;
	}
	
	public BlockFace getTurnedDirection(Block block) {
		if (!(block.getBlockData() instanceof Directional)) {
			return null;
		}
		Directional data = (Directional) block.getBlockData();
		return data.getFacing();
	}

	private void growFruit(Block block) {
		for (BlockFace face : WorldUtils.PLANAR_SIDES) {
			Block fruitBlock = block.getRelative(face);
			if (fruitBlock.getType() != Material.AIR) {
				continue;
			}
			Block below = fruitBlock.getRelative(BlockFace.DOWN);
			if (!validSoil.contains(below.getType())) {
				continue;
			}
			fruitBlock.setType(fruitMaterial, true);
			block.setType(attachedStem);
			Directional stem = (Directional) block.getBlockData();
			stem.setFacing(face);
			block.setBlockData(stem);
			return;
		}
	}

	private boolean hasPlant(Block block) {
		BlockData data = block.getBlockData();
		if (data instanceof Directional) {
			Directional dir = (Directional) data;
			return block.getRelative(dir.getFacing()).getType() == fruitMaterial;
		}
		return false;
	}

	@Override
	public boolean setStage(Plant plant, int stage) {
		if (stage == 0) {
			Block block = plant.getLocation().getBlock();
			block.setType(nonAttachedStem, true);
			BlockData data = block.getBlockData();
			if (data instanceof Ageable) {
				Ageable ageable = (Ageable) data;
				ageable.setAge(ageable.getMaximumAge());
			}
			block.setBlockData(data);
			return true;
		}
		growFruit(plant.getLocation().getBlock());
		return true;
	}

	@Override
	public boolean deleteOnFullGrowth() {
		return false;
	}

}
