package me.josvth.randomspawn.spawn;

import org.bukkit.Location;
import org.bukkit.World;

public interface SpawnSelector {

	/**
	 * Finds all the valid spawn points from the full set of configured spawn points
	 * in the world. What is valid? Spawn points can configurably require another
	 * player to be nearby, or allow spawn there regardless of nearby players. This
	 * function checks that, and checks it against the online players. If a player
	 * is sufficiently near as determined by the "checkradius", or if not set, the
	 * "radius" of the spawn point, then use that point. Of course if a nearby
	 * player is not required, the spawn point is added.
	 * <p>
	 * The final result of these checks is returned as the set of eligible spawn
	 * points; from which one will ultimately be chosen and used.
	 *
	 * @param world The world to restrict the check to. Only players from that world
	 *              are considered.
	 * @return A location near a valid spawn point.
	 * @author ProgrammerDan programmerdan@gmail.com
	 */
	Location getSpawnPointLocation(World world);

	Location getRandomSpawnLocation(World world);
}
