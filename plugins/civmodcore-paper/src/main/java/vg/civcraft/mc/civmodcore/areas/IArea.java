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
	boolean isInArea(Location loc);

	/**
	 * Collects all chunks which are (partially) inside the defined area. If the area contains an infinite amount of
	 * chunks, this will return null
	 *
	 * @return All chunks in the area or null in case of an infinite size
	 */
	Collection<Chunk> getChunks();

	/**
	 * Collects all chunks which are (partially) inside the defined area. Instead of actual chunk objects, which require
	 * the chunk behind it to be loaded, this will only return pseudo chunks, which contain the right chunk coordinates
	 * for it to be loaded later on
	 *
	 * @return All chunks in the area represented through pseudo chunks or null in case of an infinite size
	 */
	Collection<PseudoChunk> getPseudoChunks();

	/**
	 * @return Center of this area
	 */
	Location getCenter();

	/**
	 * @return World in which this area is
	 */
	World getWorld();

}
