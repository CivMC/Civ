package vg.civcraft.mc.civmodcore.areas;

import java.util.Collection;
import java.util.HashSet;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public class RectangleArea extends AbstractYLimitedArea {

	private Location center;

	private double xSize;

	private double zSize;

	public RectangleArea(double lowerYBound, double upperYBound, Location center, double xSize, double zSize) {
		super(lowerYBound, upperYBound);
		this.center = center;
		this.xSize = xSize;
		this.zSize = zSize;
	}

	@Override
	public Collection<Chunk> getChunks() {
		Collection<Chunk> chunks = new HashSet<>();
		for (double x = center.getX() - xSize; x <= center.getX() + xSize; x += 16) {
			for (double z = center.getZ() - zSize; z <= center.getZ() + zSize; z += 16) {
				chunks.add(new Location(center.getWorld(), x, center.getY(), z).getChunk());
			}
			// last one might have been skipped
			chunks.add(new Location(center.getWorld(), x, center.getY(), center.getZ() + zSize).getChunk());
		}
		return chunks;
	}

	@Override
	public boolean isInArea(Location loc) {
		double x = loc.getX();
		double z = loc.getZ();
		return loc.getWorld().getUID().equals(getWorld().getUID()) && (center.getX() - xSize) <= x
				&& (center.getX() + xSize) >= x && (center.getZ() - zSize) <= z && (center.getZ() + zSize) >= z
				&& super.isInArea(loc);
	}

	@Override
	public Location getCenter() {
		return center;
	}

	@Override
	public World getWorld() {
		return center.getWorld();
	}

	/**
	 * @return Half of the diameter of this rectangle in x dimension
	 */
	public double getXSize() {
		return xSize;
	}

	/**
	 * @return Half of the diameter of this rectangle in z dimension
	 */
	public double getZSize() {
		return zSize;
	}

	@Override
	public Collection<PseudoChunk> getPseudoChunks() {
		Collection<PseudoChunk> chunks = new HashSet<>();
		for (int x = (int) (center.getX() - xSize); x <= center.getX() + xSize; x += 16) {
			for (int z = (int) (center.getZ() - zSize); z <= center.getZ() + zSize; z += 16) {
				chunks.add(new PseudoChunk(center.getWorld(), x / 16, z / 16));
			}
			// last one might have been skipped
			chunks.add(new PseudoChunk(center.getWorld(), x / 16, ((int) (center.getZ() + zSize)) / 16));
		}
		return chunks;
	}

}
