package com.untamedears.realisticbiomes.growth;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;

public class ColumnPlantGrower extends VerticalGrower {
	
	public ColumnPlantGrower(int maxHeight, Material material, BlockFace direction, boolean instaBreakTouching) {
		super(maxHeight, material, direction, instaBreakTouching);
	}
	

}
