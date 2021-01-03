package vg.civcraft.mc.civmodcore.world;

import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public final class BeaconTracker implements Listener {

	private static final Set<Beacon> STORAGE = new HashSet<>();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkLoad(final ChunkLoadEvent event) {
		for (final BlockState tile : event.getChunk().getTileEntities()) {
			if (tile instanceof Beacon) {
				STORAGE.add((Beacon) tile);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkUnload(final ChunkUnloadEvent event) {
		for (final BlockState tile : event.getChunk().getTileEntities()) {
			if (tile instanceof Beacon) {
				STORAGE.remove(tile);
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBlockBreak(final BlockBreakEvent event) {
		final BlockState state = event.getBlock().getState();
		if (state instanceof Beacon) {
			STORAGE.remove(state);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onExplosion(final BlockExplodeEvent event) {
		for (final Block block : event.blockList()) {
			final BlockState state = block.getState();
			if (state instanceof Beacon) {
				STORAGE.remove(state);
			}
		}
	}

	// Prevent Beacons from being moved.. ultimately necessary
	// TODO: Replace this when you find a way to track beacon movements that
	//       doesn't instantly bug out immediately with a piston machine.
	@EventHandler(ignoreCancelled = true)
	public void onBlockMove(final BlockFromToEvent event) {
		final BlockState state = event.getBlock().getState();
		if (state instanceof Beacon) {
			event.setCancelled(true);
		}
	}

	/**
	 * Returns all the beacons with fields encompassing the given location.
	 *
	 * @param location The location to test.
	 * @return Returns a set of beacons that encompass the given location, which is never null.
	 */
	public static Set<Beacon> getEncompassingBeacons(final Location location) {
		Preconditions.checkArgument(WorldUtils.isValidLocation(location));
		return STORAGE.stream()
				.filter(beacon -> isInBeaconRange(beacon, location))
				.collect(Collectors.toCollection(HashSet::new));
	}

	/**
	 * Determines if a given location is within the field of a given beacon.
	 *
	 * @param beacon The beacon to test.
	 * @param location The location to test.
	 * @return Returns true if the given location is within the given beacon's field.
	 */
	public static boolean isInBeaconRange(final Beacon beacon, final Location location) {
		final Location beaconLocation = beacon.getLocation();
		final int distanceXZ = WorldUtils.blockDistance(beaconLocation, location, true);
		final double range = beacon.getEffectRange();
		if (distanceXZ < 0d || distanceXZ > range) {
			return false;
		}
		// Beacon range also extends towards, but upwards is infinite
		if (location.getY() < (beacon.getY() - range)) {
			return false;
		}
		return true;
	}

}
