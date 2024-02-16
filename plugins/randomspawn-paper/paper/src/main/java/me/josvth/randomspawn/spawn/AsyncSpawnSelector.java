package me.josvth.randomspawn.spawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 Pre-emptively loads chunks asynchronously via a queue to avoid lagging the server and loading chunks
 synchronously when a respawn is needed. It will only block if the queue has been entirely consumed.
 */
public class AsyncSpawnSelector implements SpawnSelector, Listener {

	private static final int CAPACITY = 3;

	private final BlockingSpawnSelector blocking;

	private final Plugin plugin;
	private final ConcurrentMap<String, ArrayBlockingQueue<Location>> randomSpawnLocations = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, ArrayBlockingQueue<Location>> spawnPointLocations = new ConcurrentHashMap<>();
	private final Map<String, World> worlds = new HashMap<>();

	public AsyncSpawnSelector(Plugin plugin, BlockingSpawnSelector blocking, List<String> worlds) {
		this.plugin = plugin;
		this.blocking = blocking;
		for (String world : worlds) {
			this.worlds.put(world, null);
		}
		Bukkit.getScheduler().runTaskTimer(plugin, this::cycleLocations, 20 * 60, 20 * 60);
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		for (World world : Bukkit.getWorlds()) {
			on(new WorldLoadEvent(world));
		}
	}

	private void cycleLocations() {
		for (Map.Entry<String, ArrayBlockingQueue<Location>> entry : this.randomSpawnLocations.entrySet()) {
			queueRandomSpawn(entry.getKey(), entry.getValue());
			queueSpawnPoint(entry.getKey(), entry.getValue());
		}
	}

	private void queueRandomSpawn(String worldName, ArrayBlockingQueue<Location> queue) {
		queue(worldName, queue, blocking::getRandomSpawnLocationAsync);
	}

	private void queueSpawnPoint(String worldName, ArrayBlockingQueue<Location> queue) {
		queue(worldName, queue, blocking::getSpawnPointLocationAsync);
	}

	private void queue(String worldName, ArrayBlockingQueue<Location> queue, BiFunction<World, Boolean, CompletableFuture<Location>> function) {
		Bukkit.getScheduler().runTask(plugin, () -> {
			World world = worlds.get(worldName);
			if (world == null) {
				return;
			}
			function.apply(world, false)
					.thenAccept(location -> {
						if (queue.remainingCapacity() == 0) {
							Location polled = queue.poll();
							if (polled != null) {
								polled.getChunk().removePluginChunkTicket(plugin);
							}
						}
						location.getChunk().addPluginChunkTicket(plugin);
						queue.offer(location);
						if (queue.remainingCapacity() > 0) {
							queueSpawnPoint(worldName, queue);
						}
					});
		});
	}

	@EventHandler
	public void on(WorldLoadEvent event) {
		World world = event.getWorld();
		String name = world.getName();
		if (worlds.containsKey(name)) {
			worlds.put(name, world);
			randomSpawnLocations.put(name, new ArrayBlockingQueue<>(CAPACITY));
			spawnPointLocations.put(name, new ArrayBlockingQueue<>(CAPACITY));
			cycleLocations();
		}
	}

	@EventHandler
	public void on(WorldUnloadEvent event) {
		World world = event.getWorld();
		String name = world.getName();
		if (worlds.containsKey(name)) {
			worlds.put(name, null);
			randomSpawnLocations.remove(name);
			spawnPointLocations.remove(name);
		}
	}

	private Location getLocation(World world, BiConsumer<String, ArrayBlockingQueue<Location>> queueFunc, Function<World, Location> getFunc, Map<String, ArrayBlockingQueue<Location>> locations) {
		if (worlds.get(world.getName()) == null) {
			return null;
		}

		ArrayBlockingQueue<Location> queue = locations.get(world.getName());
		Location spawnPoint = queue.poll();
		queueFunc.accept(world.getName(), queue);
		if (spawnPoint != null) {
			spawnPoint.getChunk().removePluginChunkTicket(plugin);
			return spawnPoint;
		}

		return getFunc.apply(world);
	}

	@Override
	public Location getSpawnPointLocation(World world) {
		return getLocation(world, this::queueSpawnPoint, blocking::getSpawnPointLocation, spawnPointLocations);
	}

	@Override
	public Location getRandomSpawnLocation(World world) {
		return getLocation(world, this::queueRandomSpawn, blocking::getRandomSpawnLocation, randomSpawnLocations);
	}
}
