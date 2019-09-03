package vg.civcraft.mc.civmodcore.areas;

import java.util.Collection;
import java.util.HashSet;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public class EllipseArea extends AbstractYLimitedArea {

	private Location center;

	private double xSize;

	private double zSize;

	public EllipseArea(double lowerYBound, double upperYBound, Location center, double xSize, double zSize) {
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
				Chunk c = new Location(center.getWorld(), x, center.getY(), z).getChunk();
				// if one of the corners is in the area the chunk is inside
				if (isInArea(new Location(c.getWorld(), c.getX() * 16, 0, (c.getZ() * 16) + 15))
						|| isInArea(new Location(c.getWorld(), c.getX() * 16, 0, c.getZ() * 16))
						|| isInArea(new Location(c.getWorld(), (c.getX() * 16) + 15, 0, c.getZ() * 16))
						|| isInArea(new Location(c.getWorld(), (c.getX() * 16) + 15, 0, (c.getZ() * 16) + 15))) {
					chunks.add(c);
				}
			}
		}
		return chunks;
	}

	@Override
	public Location getCenter() {
		return center;
	}

	@Override
	public World getWorld() {
		return center.getWorld();
	}

	@Override
	public boolean isInArea(Location loc) {
		double xDist = center.getX() - loc.getX();
		double zDist = center.getZ() - loc.getZ();
		return super.isInArea(loc) && ((xDist * xDist) / (xSize * xSize)) + ((zDist * zDist) / (zSize * zSize)) <= 1;
	}

	/**
	 * @return Half of the diameter of this ellipse in x dimension
	 */
	public double getXSize() {
		return xSize;
	}

	/**
	 * @return Half of the diameter of this ellipse in z dimension
	 */
	public double getZSize() {
		return zSize;
	}

	@Override
	public Collection<PseudoChunk> getPseudoChunks() {
		Collection<PseudoChunk> chunks = new HashSet<>();
		for (int x = (int) (center.getX() - xSize); x <= center.getX() + xSize; x += 16) {
			for (int z = (int) (center.getZ() - zSize); z <= center.getZ() + zSize; z += 16) {
				PseudoChunk c = new PseudoChunk(center.getWorld(), x / 16, z / 16);
				// if one of the corners is in the area the chunk is inside
				if (isInArea(new Location(c.getWorld(), c.getX() * 16, 0, (c.getZ() * 16) + 15))
						|| isInArea(new Location(c.getWorld(), c.getX() * 16, 0, c.getZ() * 16))
						|| isInArea(new Location(c.getWorld(), (c.getX() * 16) + 15, 0, c.getZ() * 16))
						|| isInArea(new Location(c.getWorld(), (c.getX() * 16) + 15, 0, (c.getZ() * 16) + 15))) {
					chunks.add(c);
				}
			}
		}
		return chunks;
	}

}
