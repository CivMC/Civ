package com.untamedears.realisticbiomes.model.gauss;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.Region;
import com.untamedears.realisticbiomes.model.ltree.BlockTransformation;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public class GaussTree {

	private GaussProperty preCanopyHeight;
	private GaussProperty stemAngleStep;
	private double stemAngleCap;
	private GaussProperty stemRadius;
	private BlockTransformation stemTransform;
	private BlockTransformation leafTransform;

	private GaussProperty canopyHeight;
	private GaussProperty leafAmount;
	private double branchChance;
	private GaussProperty branchLength;
	private GaussProperty branchLogInterval;
	private GaussProperty branchAngle;

	private Random rng;

	public GaussTree(ItemStack sapling) {

	}

	public GaussTree(GaussProperty preCanopyHeight, GaussProperty stemAngleStep, double stemAngleCap,
			GaussProperty stemRadius, BlockTransformation stemTransform, GaussProperty canopyHeight,
			GaussProperty leafAmount, double branchChance, GaussProperty branchLength, GaussProperty branchLogInterval,
			GaussProperty branchAngle, BlockTransformation leafTransform) {
		this.preCanopyHeight = preCanopyHeight;
		this.stemAngleStep = stemAngleStep;
		this.stemAngleCap = stemAngleCap;
		this.stemRadius = stemRadius;
		this.stemTransform = stemTransform;
		this.canopyHeight = canopyHeight;
		this.leafAmount = leafAmount;
		this.branchChance = branchChance;
		this.branchLength = branchLength;
		this.branchLogInterval = branchLogInterval;
		this.branchAngle = branchAngle;
		this.leafTransform = leafTransform;
		rng = new Random();
	}

	public void genAt(Location loc) {
		loc = new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY() + 0.5, loc.getBlockZ() + 0.5);
		loc = genTreeSection(loc, preCanopyHeight, false);
		loc = genTreeSection(loc, canopyHeight, true);
	}

	private Location genTreeSection(Location startingLocation, GaussProperty sectionHeight, boolean branch) {
		int localHeight = sectionHeight.getRoundedRandomValue();
		Vector up = new Vector(0, 1, 0);
		Vector direction = up.clone();
		double radius = stemRadius.getRandomValue();
		Location currentLocation = startingLocation.clone();
		for (int i = 0; i < localHeight; i++) {
			// first set blocks for the current location
			List<Block> logs = setNearby(currentLocation, direction, radius, stemTransform);

			if (branch) {
				// second generate eventual branches
				logs.addAll(generateBranches(currentLocation, branchChance));

				// third generate foliage around it
				for (Block log : logs) {
					double localChance = leafAmount.getRandomValue();
					recursiveLeafGeneration(log, localChance);
				}
			}

			// fourth apply a random directional deviation to the upwards movement

			// calculate length of the direction vector added
			double deviation = stemAngleStep.getRandomValue();
			// vector in random x z direction
			Vector randomDirection = new Vector(randomNumber(-1, 1), 0, randomNumber(-1, 1)).normalize()
					.multiply(deviation);
			Vector newDirection = direction.clone().add(randomDirection).normalize();
			double dotProductUp = up.dot(newDirection);
			if (dotProductUp > stemAngleCap) {
				// stemAngleCap works as lower cap on this dot product, which is the cosine of
				// the angle to the y-axis
				direction = newDirection;
			}
			currentLocation.add(direction);
		}
		return currentLocation;
	}

	private Vector getRandomXZVector() {
		return new Vector(randomNumber(-1, 1), 0, randomNumber(-1, 1));
	}

	private void recursiveLeafGeneration(Block sourceBlock, double chance) {
		if (chance >= 1.0 || Math.random() < chance) {
			for (Block innerBlock : WorldUtils.getAllBlockSides(sourceBlock, true)) {
				if (leafTransform.applyAt(innerBlock.getLocation())) {
					recursiveLeafGeneration(innerBlock, chance - 1);
				}
			}
		}
	}

	private List<Block> generateBranches(Location sourceLocation, double chance) {
		List<Block> result = new ArrayList<>();
		while (chance >= 1.0 || rng.nextDouble() <= branchChance) {
			generateBranch(sourceLocation, result);
			chance--;
			if (chance <= 0) {
				break;
			}
		}
		return result;
	}

	private void generateBranch(Location sourceLocation, List<Block> result) {
		Vector direction = getRandomXZVector();
		Vector oppositeDirection = direction.multiply(-1);
		Vector yModifier = new Vector(0, branchAngle.getRandomValue(), 0);
		direction.add(yModifier);
		oppositeDirection.add(yModifier);
		direction.normalize();
		oppositeDirection.normalize();
		double length = branchLength.getRandomValue();
		double logInterval = branchLogInterval.getRandomValue();
		int blockAmount = (int) (length / logInterval);
		Location currentLocation = sourceLocation.clone();
		Location oppositeLocation = sourceLocation.clone();
		Vector perStepOffset = direction.clone().multiply(logInterval);
		Vector oppositeStepOffset = oppositeDirection.clone().multiply(logInterval);
		for (int i = 0; i < blockAmount; i++) {
			currentLocation.add(perStepOffset);
			oppositeLocation.add(oppositeStepOffset);
			stemTransform.applyAt(currentLocation);
			stemTransform.applyAt(oppositeLocation);
			result.add(currentLocation.getBlock());
			result.add(oppositeLocation.getBlock());
		}
	}

	private List<Block> setNearby(Location center, Vector direction, double radius, BlockTransformation transformer) {
		List<Block> result = new ArrayList<>();
		Region region = new CylinderRegion(BukkitAdapter.adapt(center.getWorld()), BukkitAdapter.asBlockVector(center),
				Vector2.at(radius, radius), center.getBlockY(), center.getBlockY());
		region.forEach(b -> {
			Block block = BukkitAdapter.adapt(center.getWorld(), b).getBlock();
			transformer.applyAt(block);
			result.add(block);
		});
		return result;
	}

	private double randomNumber(double lowerBound, double upperBound) {
		double range = upperBound - lowerBound;
		return rng.nextDouble() * range + lowerBound;
	}
}
