package com.untamedears.realisticbiomes.growth;

import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Sapling;

import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;

public class TreeGrower extends AgeableGrower {

	public TreeGrower() {
		super(1, 1);
	}
	
	@Override
	public int getStage(Block block) {
		if (!(block.getBlockData() instanceof Sapling)) {
			return -1;
		}
		return 0;
	}
	
	@Override
	public void setPersistentProgress(PlantGrowthConfig config, Block block, double progress) {
		if (progress < 1.0) {
			return;
		}
		if (!(block.getBlockData() instanceof Sapling)) {
			return;
		}
		Sapling sapling = (Sapling) block.getBlockData();
		sapling.get
		growOnTop(block, (int) progress);		
	}

	@Override
	public void setStage(Block block, int stage) {
		if (!(block.getBlockData() instanceof Ageable)) {
			throw new IllegalArgumentException("Can not set age for non Ageable");
		}
		Ageable ageable = ((Ageable) block.getBlockData());
		ageable.setAge(stage);
		block.setBlockData(ageable, true);
	}
	
	private void growTree(Block block) {
		block.getLocation().getWorld().generateTree(block.getLocation(), TreeType.)
	}

	

}
