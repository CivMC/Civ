package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.world.locations.global.GlobalLocationTracker;
import vg.civcraft.mc.civmodcore.world.locations.global.LocationTrackable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SingleBlockAPIView <T extends LocationTrackable> extends APIView {

	private static final long REGULAR_SAVE_INTERVAL_MILLISECONDS = 60L * 1000L;

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final GlobalLocationTracker<T> tracker;
	private ScheduledFuture<?> regularSaveRunnable;
	
	SingleBlockAPIView(JavaPlugin plugin, short pluginID, GlobalLocationTracker<T> tracker) {
		super(plugin, pluginID);
		this.tracker = tracker;
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, tracker::initFromDB);
		registerRegularSaveRunnable();
	}

	private void registerRegularSaveRunnable() {
		this.regularSaveRunnable = scheduler.scheduleWithFixedDelay(() -> {
			tracker.persist();
		}, REGULAR_SAVE_INTERVAL_MILLISECONDS, REGULAR_SAVE_INTERVAL_MILLISECONDS, TimeUnit.MILLISECONDS);
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
		if (this.regularSaveRunnable != null)
			this.regularSaveRunnable.cancel(false);

		tracker.persist();
	}

}
