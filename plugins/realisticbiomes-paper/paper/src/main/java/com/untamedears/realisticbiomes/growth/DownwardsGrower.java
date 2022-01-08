package com.untamedears.realisticbiomes.growth;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;

public class DownwardsGrower extends VerticalGrower {
	
	public DownwardsGrower(int maxHeight, Material material) {
		super(maxHeight, material, BlockFace.DOWN, false);
	}
	

}
