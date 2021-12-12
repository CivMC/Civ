package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.world.locations.global.GlobalLocationTracker;
import vg.civcraft.mc.civmodcore.world.locations.global.LocationTrackable;

public class SingleBlockAPIView <T extends LocationTrackable> extends APIView {

	private final GlobalLocationTracker<T> tracker;
	
	SingleBlockAPIView(JavaPlugin plugin, short pluginID, GlobalLocationTracker<T> tracker) {
		super(plugin, pluginID);
		this.tracker = tracker;
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, tracker::initFromDB);
	}
	
	public T get(Location loc) {
		return tracker.get(loc);
	}

	public void put(T trackable) {
		tracker.put(trackable);
	}

	public T remove(Location loc) {
		return tracker.remove(loc);
	}

	public T remove(T trackable) {
		return remove(trackable.getLocation());
	}

	public void handleChunkLoad(Chunk chunk) {

	}

	public void handleChunkUnload(Chunk chunk) {

	}

	@Override
	public void disable() {
		tracker.persist();
	}

}
