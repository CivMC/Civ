package vg.civcraft.mc.civmodcore.world;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 * This tracker should provide an efficient way of only using worlds that are loaded in.
 */
public class WorldTracker implements Listener {

	private static final Map<UUID, World> STORAGE = new HashMap<>();

	@EventHandler(priority = EventPriority.LOWEST)
	public void onWorldLoad(final WorldLoadEvent event) {
		final World world = event.getWorld();
		STORAGE.put(world.getUID(), world);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onWorldUnload(final WorldUnloadEvent event) {
		final World world = event.getWorld();
		STORAGE.remove(world.getUID());
	}

	/**
	 * Retrieves the {@link World} object for the given world {@link UUID}.
	 *
	 * @param uuid The world's {@link UUID}.
	 * @return Returns the world's instance or null if it's not loaded.
	 */
	public static World getLoadedWorld(final UUID uuid) {
		return STORAGE.get(uuid);
	}

	/**
	 * Resets the tracker. Should only be used when disabling CivModCore.
	 */
	public static void reset() {
		STORAGE.clear();
	}

}
