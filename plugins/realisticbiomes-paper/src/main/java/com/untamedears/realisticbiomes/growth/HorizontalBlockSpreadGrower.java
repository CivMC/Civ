package com.untamedears.realisticbiomes.growth;

import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.untamedears.realisticbiomes.model.Plant;

public class HorizontalBlockSpreadGrower extends IArtificialGrower {

	private static final Random rng = new Random();

	private final int maximumInArea;
	private final int maximumRange;
	private Material material;
	private Set<Material> validSoil;

	public HorizontalBlockSpreadGrower(Material material, int maximumInArea, int maximumRange, Set<Material> validSoil) {
		this.maximumInArea = maximumInArea;
		this.maximumRange = maximumRange;
		this.validSoil = validSoil;
		this.material = material;
	}

	@Override
	public int getIncrementPerStage() {
		return 1;
	}

	@Override
	public int getMaxStage() {
		return maximumInArea - 1;
	}

	@Override
	public int getStage(Plant plant) {
		Block block = plant.getLocation().getBlock();
		if (block.getType() != material) {
			return -1;
		}
		int maxX = block.getX() + maximumRange;
		int maxZ = block.getZ() + maximumRange;
		int count = 0;
		for (int x = block.getX() - maximumRange; x < maxX; x++) {
			for (int z = block.getZ() - maximumRange; z < maxZ; z++) {
				if (block.getWorld().getBlockAt(x, block.getY(), z).getType() == material) {
					count++;
				}
			}
		}
		// remove block itself which is always there, because stage 0 equals only the
		// center block itself
		return count - 1;
	}

	@Override
	public void setStage(Plant plant, int stage) {
		// pick a random spot in range, scan forward in lines from it, then scan
		// backwards in lines from it
		int xOffset = rng.nextInt(maximumRange * 2 + 1);
		int zOffset = rng.nextInt(maximumRange * 2 + 1);
		Location lowerCorner = plant.getLocation().clone();
		lowerCorner.subtract(maximumRange, 0, maximumRange);
		int upperXBound = plant.getLocation().getBlockX() + maximumRange;
		int upperZBound = plant.getLocation().getBlockZ() + maximumRange;
		// starting line forward
		for (int z = zOffset + lowerCorner.getBlockZ(); z <= upperZBound; z++) {
			if (growAtSpot(new Location(lowerCorner.getWorld(), lowerCorner.getBlockX() + xOffset, lowerCorner.getBlockY(),
					z))) {
				return;
			}
		}
		//scan forward
		for (int x = xOffset + lowerCorner.getBlockX() + 1; x <= upperXBound; x++) {
			for (int z = zOffset + lowerCorner.getBlockZ(); z <= upperZBound; z++) {
				if (growAtSpot(new Location(lowerCorner.getWorld(), x, lowerCorner.getBlockY(),
						z))) {
					return;
				}
			}
		}
		//starting line backward
		for (int z = zOffset + lowerCorner.getBlockZ() - 1; z >= lowerCorner.getBlockZ() ; z--) {
			if (growAtSpot(new Location(lowerCorner.getWorld(), lowerCorner.getBlockX() + xOffset, lowerCorner.getBlockY(),
					z))) {
				return;
			}
		}
		//scan backward
		for (int x = xOffset + lowerCorner.getBlockX() - 1; x >= lowerCorner.getBlockX(); x--) {
			for (int z = zOffset + lowerCorner.getBlockZ(); z >= lowerCorner.getBlockZ(); z--) {
				if (growAtSpot(new Location(lowerCorner.getWorld(), x, lowerCorner.getBlockY(),
						z))) {
					return;
				}
			}
		}

	}

	private boolean growAtSpot(Location loc) {
		Block block = loc.getBlock();
		if (!validSoil.isEmpty()) {
			Block below = block.getRelative(BlockFace.DOWN);
			if (!validSoil.contains(below.getType())) {
				return false;
			}
		}
		block.setType(material);
		return true;
	}

	@Override
	public boolean deleteOnFullGrowth() {
		return false;
	}

}
