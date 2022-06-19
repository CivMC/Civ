package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.stat;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoadStatisticManager {
	public static final int MainThreadIndex = -1;
	private static final long PollIntervalMilliseconds = 1000L;

	private static class Action {
		public final UUID worldId;
		public final int threadIndex;
		public final short pluginId;
		public final long timeNanoSec;
		public final boolean isStarted;

		public Action(UUID worldId, int threadIndex, short pluginId, long timeNanoSec, boolean isStarted) {
			this.worldId = worldId;
			this.threadIndex = threadIndex;
			this.pluginId = pluginId;
			this.timeNanoSec = timeNanoSec;
			this.isStarted = isStarted;
		}
	}

	private static LoadStatisticManager instance;

	public static void enable() {
		instance = new LoadStatisticManager();
		instance.startPolling();
	}

	public static void disable() {
		if (instance == null)
			return;

		instance.stopPolling();
		instance = null;
	}

	public static void registerPlugin(String name, short id) {
		if (instance == null)
			return;

		instance.plugins.put(id, new PluginStatistic(id, name));
	}

	public static void start(World world, int threadIndex, short pluginId) {
		if (instance == null)
			return;

		Action action = new Action(world.getUID(), threadIndex, pluginId, System.nanoTime(), true);
		instance.actions.add(action);
	}

	public static void stop(World world, int threadIndex, short pluginId) {
		if (instance == null)
			return;

		Action action = new Action(world.getUID(), threadIndex, pluginId, System.nanoTime(), false);
		instance.actions.add(action);
	}

	public static LoadStatistic getLoadStatistic() {
		if (instance == null)
			return null;

		instance.poll();

		return instance.getLoadStatisticInternal();
	}

	private final ConcurrentLinkedQueue<Action> actions;
	private final Map<UUID, Map<Integer, Action>> worlds;
	private final Map<Short, PluginStatistic> plugins;
	private final ScheduledExecutorService scheduler;

	private LoadStatisticManager() {
		this.actions = new ConcurrentLinkedQueue<>();
		this.worlds = new HashMap<>();
		this.plugins = new HashMap<>();
		this.scheduler = Executors.newScheduledThreadPool(1);
	}

	private void startPolling() {
		scheduler.scheduleWithFixedDelay(() -> {
			poll();
		}, PollIntervalMilliseconds, PollIntervalMilliseconds, TimeUnit.MILLISECONDS);
	}

	private void stopPolling() {
		this.scheduler.shutdown();

		try {
			if (!this.scheduler.awaitTermination(5, TimeUnit.SECONDS))
				this.scheduler.shutdownNow();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	private synchronized LoadStatistic getLoadStatisticInternal() {
		List<PluginStatistic> pluginStatistics = new ArrayList<>();
		for (PluginStatistic statistic : plugins.values())
			pluginStatistics.add(statistic.clone());

		Collections.sort(pluginStatistics, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.pluginName, b.pluginName));

		int threadCount = 0;

		List<LoadStatistic.WorldThreads> worldThreadsList = new ArrayList<>();
		for (UUID worldId : this.worlds.keySet()) {
			Map<Integer, Action> worldActions = this.worlds.get(worldId);

			threadCount += worldActions.size();

			LoadStatistic.WorldThreads worldThreads = createWorldThreads(worldId, worldActions);

			if (worldThreads != null)
				worldThreadsList.add(worldThreads);
		}

		Collections.sort(worldThreadsList, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.world.getName(), b.world.getName()));

		return new LoadStatistic(this.worlds.size(), threadCount,  worldThreadsList, pluginStatistics);
	}

	private LoadStatistic.WorldThreads createWorldThreads(UUID worldId, Map<Integer, Action> worldActions) {
		Action mainThreadAction = worldActions.get(MainThreadIndex);
		Long mainThreadTime = mainThreadAction != null ? System.nanoTime() - mainThreadAction.timeNanoSec : null;

		World world = Bukkit.getWorld(worldId);
		LoadStatistic.WorldThreads worldThreads = new LoadStatistic.WorldThreads(world, mainThreadTime, new ArrayList<>());

		for (Action threadAction : worldActions.values()) {
			if (threadAction != null && threadAction.threadIndex != MainThreadIndex) {
				Long threadTime = System.nanoTime() - threadAction.timeNanoSec;
				worldThreads.threadTimes.add(new LoadStatistic.ThreadTime(threadAction.threadIndex, threadTime));
			}
		}

		if (mainThreadTime == null && worldThreads.threadTimes.size() == 0)
			return null;

		Collections.sort(worldThreads.threadTimes, Comparator.comparingInt(a -> a.threadIndex));

		return worldThreads;
	}

	private synchronized void poll() {
		Action current;
		while ((current = actions.poll()) != null) {
			Map<Integer, Action> worldActions = this.worlds.get(current.worldId);
			if (worldActions == null) {
				worldActions = new HashMap<>();
				this.worlds.put(current.worldId, worldActions);
			}

			Action prev = worldActions.get(current.threadIndex);

			if (current.isStarted) {
				worldActions.put(current.threadIndex, current);
			} else {
				worldActions.put(current.threadIndex, null);

				if (prev != null) {
					addStatistic(prev, current);
				}
			}
		}
	}

	private void addStatistic(Action prev, Action current) {
		if (prev.pluginId != current.pluginId)
			return;

		long time = current.timeNanoSec - prev.timeNanoSec;

		PluginStatistic statistic = this.plugins.get(prev.pluginId);
		statistic.chunkLoadCount++;
		statistic.chunkLoadSumNanoSec += time;

		if (statistic.isInitialized) {
			if (statistic.chunkLoadMinTimeNanoSec > time)
				statistic.chunkLoadMinTimeNanoSec = time;

			if (statistic.chunkLoadMaxTimeNanoSec < time)
				statistic.chunkLoadMaxTimeNanoSec = time;
		} else {
			statistic.chunkLoadMinTimeNanoSec = time;
			statistic.chunkLoadMaxTimeNanoSec = time;
			statistic.isInitialized = true;
		}
	}
}
