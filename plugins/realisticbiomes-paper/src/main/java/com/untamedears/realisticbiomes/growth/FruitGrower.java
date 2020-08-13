package com.untamedears.realisticbiomes.growth;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

import com.untamedears.realisticbiomes.model.Plant;

import vg.civcraft.mc.civmodcore.api.BlockAPI;

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
			block.setType(nonAttachedStem); // doesn't have a fruit, but is attached type, so fix
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
		for (BlockFace face : BlockAPI.PLANAR_SIDES) {
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
	public void setStage(Plant plant, int stage) {
		if (stage == 0) {
			Block block = plant.getLocation().getBlock();
			block.setType(nonAttachedStem, true);
			if (block.getBlockData() instanceof Ageable) {
				Ageable ageable = (Ageable) block.getBlockData();
				ageable.setAge(ageable.getMaximumAge());
			}
			return;
		}
		growFruit(plant.getLocation().getBlock());
	}

	@Override
	public boolean deleteOnFullGrowth() {
		return false;
	}

}
