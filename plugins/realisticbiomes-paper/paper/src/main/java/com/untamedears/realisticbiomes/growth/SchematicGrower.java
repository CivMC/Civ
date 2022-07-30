package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.model.Plant;
import com.untamedears.realisticbiomes.model.RBSchematic;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class SchematicGrower extends IArtificialGrower {
	
	private RBSchematic schematic;
	private Location offset;
	
	public SchematicGrower(RBSchematic schematic, Location offSet) {
		this.schematic = schematic;
		this.offset = offSet;
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
		if (block.getType() != plant.getGrowthConfig().getItem().getType()) {
			return -1;
		}
		return 0;
	}

	@Override
	public boolean setStage(Plant plant, int stage) {
		if (stage < 1) {
			return true;
		}
		schematic.spawnAt(plant.getLocation().add(offset));
		return true;
	}

	@Override
	public boolean deleteOnFullGrowth() {
		return true;
	}

}
