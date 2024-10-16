package com.untamedears.realisticbiomes.growth;

import com.google.common.base.Preconditions;
import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.model.Plant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class HorizontalBlockSpreadGrower extends IArtificialGrower {

	private static final Random rng = new Random();

	private final int maximumInArea;
	private final int maximumRange;
	private Material material;
	private Set<Material> validSoil;
	private Set<Material> replaceableBlocks;

	public HorizontalBlockSpreadGrower(Material material, int maximumInArea, int maximumRange,
			Collection<Material> replaceableBlocks, Collection<Material> validSoil) {
		Preconditions.checkNotNull(material);
		Preconditions.checkNotNull(replaceableBlocks);
		Preconditions.checkNotNull(validSoil);
		this.maximumInArea = maximumInArea;
		this.maximumRange = maximumRange;
		this.validSoil = EnumSet.copyOf(validSoil);
		this.replaceableBlocks = EnumSet.copyOf(replaceableBlocks);
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
	public boolean setStage(Plant plant, int stage) {
		int currentAmount = getStage(plant);
		int toAdd = stage - currentAmount;
		outer:
		for (int i = 0; i < toAdd; i++) {
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
				if (growAtSpot(plant, new Location(lowerCorner.getWorld(), lowerCorner.getBlockX() + xOffset,
						lowerCorner.getBlockY(), z))) {
					continue outer;
				}
			}
			// scan forward
			for (int x = xOffset + lowerCorner.getBlockX() + 1; x <= upperXBound; x++) {
				for (int z = zOffset + lowerCorner.getBlockZ(); z <= upperZBound; z++) {
					if (growAtSpot(plant, new Location(lowerCorner.getWorld(), x, lowerCorner.getBlockY(), z))) {
						continue outer;
					}
				}
			}
			// starting line backward
			for (int z = zOffset + lowerCorner.getBlockZ() - 1; z >= lowerCorner.getBlockZ(); z--) {
				if (growAtSpot(plant, new Location(lowerCorner.getWorld(), lowerCorner.getBlockX() + xOffset,
						lowerCorner.getBlockY(), z))) {
					continue outer;
				}
			}
			// scan backward
			for (int x = xOffset + lowerCorner.getBlockX() - 1; x >= lowerCorner.getBlockX(); x--) {
				for (int z = zOffset + lowerCorner.getBlockZ(); z >= lowerCorner.getBlockZ(); z--) {
					if (growAtSpot(plant, new Location(lowerCorner.getWorld(), x, lowerCorner.getBlockY(), z))) {
						continue outer;
					}
				}
			}
			break; // no valid growth spot available, cancel all further growth attempts
		}

		return true;
	}

	private boolean growAtSpot(Plant source, Location loc) {
		Block block = loc.getBlock();
		if (!replaceableBlocks.isEmpty()) {
			if (!replaceableBlocks.contains(block.getType())) {
				return false;
			}
		}
		if (!validSoil.isEmpty()) {
			Block below = block.getRelative(BlockFace.DOWN);
			if (!validSoil.contains(below.getType())) {
				return false;
			}
		}
		block.setType(material);
		// create new plant at this location for further spread
		Plant plant = new Plant(block.getLocation(), source.getGrowthConfig());
		RealisticBiomes.getInstance().getPlantManager().putPlant(plant);
		RealisticBiomes.getInstance().getPlantLogicManager().updateGrowthTime(plant, block);
		return true;
	}

	@Override
	public boolean deleteOnFullGrowth() {
		return false;
	}

}
