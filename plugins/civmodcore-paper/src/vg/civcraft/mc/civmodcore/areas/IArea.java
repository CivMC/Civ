package vg.civcraft.mc.civmodcore.areas;

import java.util.Collection;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public interface IArea {
	/**
	 * Checks whether a given location is inside the defined area
	 * 
	 * @param loc
	 *            Location to check
	 * @return True if the given location is inside the area, false if not
	 */
	public boolean isInArea(Location loc);

	/**
	 * Collects all chunks which are (partially) inside the defined area. If
	 * the area contains an infinite amount of chunks, this will return null
	 * 
	 * @return All chunks in the area or null in case of an infinite size
	 */
	public Collection<Chunk> getChunks();

	/**
	 * @return Center of this area
	 */
	public Location getCenter();

	/**
	 * @return World in which this area is
	 */
	public World getWorld();
}
