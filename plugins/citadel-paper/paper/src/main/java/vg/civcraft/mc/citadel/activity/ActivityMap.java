package vg.civcraft.mc.citadel.activity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.citadel.model.ActivityDB;
import vg.civcraft.mc.citadel.model.ActivityItem;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.world.locations.global.WorldIDManager;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class ActivityMap {

	private static final long SAVE_CHANGES_INTERVAL_MS = 60L * 1000L; // 1 min
	private static final long UNLOAD_INTERVAL_MS = 60L * 1000L; // 1 min

	private final Logger logger;
	private final ActivityDB activityDB;
	private final ScheduledExecutorService scheduler;
	private AtomicBoolean loadChunkDisabled;
	private Thread loadChunkThread;
	private final WorldIDManager worldIdManager;
	private final Map<Short, Map<XZKey, RegionData>> data; // world, xz, group, activity/entry
	private final ConcurrentLinkedQueue<PlayerUpdate> playerUpdates;
	private final LinkedBlockingQueue<ChunkCoord> chunkLoadQueue;
	private final ConcurrentLinkedQueue<ChunkCoord> chunkUnloadQueue;
	private final ActivityMapTimePoll timePoll;
	private final AtomicInteger unloadCount;

	private final Set<UUID> worlds;
	private final int resolution;
	private final int radius;
	private final long entryRefreshAfterMs;
	private final long radiusRefreshAfterMs;
	private final Instant defaultActivity;

	private boolean enabled;

	public ActivityMap(Logger logger, ManagedDatasource source) {
		this.logger = logger;
		this.activityDB = new ActivityDB(source);
		this.scheduler = Executors.newScheduledThreadPool(2);
		this.worldIdManager = CivModCorePlugin.getInstance().getWorldIdManager();
		this.data = new ConcurrentHashMap<>();
		this.playerUpdates = new ConcurrentLinkedQueue<>();
		this.chunkLoadQueue = new LinkedBlockingQueue<>();
		this.chunkUnloadQueue = new ConcurrentLinkedQueue<>();
		this.timePoll = new ActivityMapTimePoll();
		this.unloadCount = new AtomicInteger();

		int resolution = 16 * (Citadel.getInstance().getConfigManager().getActivityMapResolution() / 16);
		this.resolution = resolution <= 0 ? 1 : resolution;

		this.radius = Citadel.getInstance().getConfigManager().getActivityMapRadius();
		this.entryRefreshAfterMs = Citadel.getInstance().getConfigManager().getActivityEntryRefreshAfterMs();
		this.radiusRefreshAfterMs = Citadel.getInstance().getConfigManager().getActivityRadiusRefreshAfterMs();
		this.worlds = new HashSet<>();
		this.defaultActivity = Instant
				.ofEpochSecond(Citadel.getInstance().getConfigManager().getActivityDefault());

		this.enabled = false;
	}

	public void enable() {
		if (!activityDB.enable()) {
			logger.severe("ActivityMap cannot be enabled");
			return;
		}

		for (String world : Citadel.getInstance().getConfigManager().getActivityWorlds()) {
			World bukkitWorld = Bukkit.getWorld(world);
			if (bukkitWorld == null) {
				Citadel.getInstance().getLogger().warning("World not found: " + world);
			} else {
				worlds.add(bukkitWorld.getUID());
			}
		}

		scheduler.scheduleWithFixedDelay(() -> {
			saveChangesToDB();
		}, SAVE_CHANGES_INTERVAL_MS, SAVE_CHANGES_INTERVAL_MS, TimeUnit.MILLISECONDS);

		scheduler.scheduleWithFixedDelay(() -> {
			unloadChunkTask();
		}, UNLOAD_INTERVAL_MS, UNLOAD_INTERVAL_MS, TimeUnit.MILLISECONDS);

		startLoadChunkThread();

		timePoll.startPolling();

		enabled = true;
	}

	private void startLoadChunkThread() {
		loadChunkDisabled = new AtomicBoolean(false);

		final String threadName = "citadel-activity-map-chunk-loading";
		loadChunkThread = new Thread(() -> loadChunkThreadTask(threadName, loadChunkDisabled), threadName);
		loadChunkThread.start();
	}

	public void disable() {
		if (!enabled) {
			return;
		}

		loadChunkDisabled.set(true);
		loadChunkThread.interrupt();

		scheduler.shutdown();

		try {
			if (!scheduler.awaitTermination(60, TimeUnit.SECONDS))
				scheduler.shutdownNow();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		timePoll.stopPolling();

		logger.info("ActivityMap scheduler and its tasks are shutdown.");

		saveChangesToDB();

		logger.info("ActivityMap changes are saved.");
	}

	private Instant getActivity(RegionCoord regionCoord, int groupId) {
		RegionData regionData = getRegion(regionCoord, true);

		synchronized (regionData) {
			GroupData groupData = regionData.get(groupId);
			Instant activity = groupData != null ? groupData.activity : null;
			return activity;
		}
	}

	private RegionData getRegion(RegionCoord regionCoord, boolean loadIfNotLoaded) {
		Map<XZKey, RegionData> worldData = data.computeIfAbsent(regionCoord.worldId(), a -> new ConcurrentHashMap<>());
		XZKey regionKey = new XZKey(regionCoord.x(), regionCoord.z());
		RegionData regionData = worldData.computeIfAbsent(regionKey, a -> new RegionData());

		if (regionData.isLoaded() || !loadIfNotLoaded) {
			return regionData;
		}

		synchronized (regionData) {
			if (regionData.isLoaded()) {
				return regionData;
			}

			long start = System.nanoTime();

			activityDB.select(regionCoord.worldId(), regionCoord.x(), regionCoord.z(), resolution, activityItem -> {
				GroupData groupData = new GroupData(activityItem.activity());
				regionData.put(activityItem.group(), groupData);
			});

			timePoll.pushTimeNano(System.nanoTime() - start);

			regionData.setLoaded();

			return regionData;
		}
	}

	private void saveChangesToDB() {
		Map<RegionCoord, Set<Integer>> regions = getUpdatedRegions();
		if (regions == null || regions.size() == 0) {
			return;
		}

		Map<RegionCoord, Set<Integer>> extendedRegions = getExtendedRegions(regions);
		List<ActivityItem> activities = new ArrayList<>();

		for (Map.Entry<RegionCoord, Set<Integer>> regionEntry : extendedRegions.entrySet()) {
			RegionCoord regionCoord = regionEntry.getKey();
			Set<Integer> groups = regionEntry.getValue();
			Instant activity = Instant.now();

			getUpdatedActivities(activities, regionCoord, groups, activity);

			boolean updateEntry = regions.containsKey(regionCoord);
			RegionData regionData = getRegion(regionCoord, false);

			if (!regionData.isLoaded()) {
				continue;
			}

			synchronized (regionData) {
				for (int groupId : groups) {
					GroupData groupData = regionData.get(groupId);
					groupData.activity = activity;

					if (updateEntry) {
						groupData.entry = activity;
					}
				}
			}
		}

		activityDB.update(activities);
	}

	private Map<RegionCoord, Set<Integer>> getUpdatedRegions() {
		GroupManager groupManager = NameAPI.getGroupManager();
		Map<RegionCoord, Set<Integer>> regions = null;

		PlayerUpdate playerUpdate;
		while ((playerUpdate = playerUpdates.poll()) != null) {
			if (regions == null) {
				regions = new HashMap<>();
			}

			Set<Integer> groups = regions.computeIfAbsent(playerUpdate.regionCoord(), a -> new HashSet<>());

			List<String> groupNames = groupManager.getAllGroupNames(playerUpdate.playerId());
			for (String groupName : groupNames) {
				Group group = GroupManager.getGroup(groupName);
				if (groupManager.hasAccess(group, playerUpdate.playerId(), CitadelPermissionHandler.getBypass())) {
					groups.add(group.getGroupId());
				}
			}
		}

		return regions;
	}

	private Map<RegionCoord, Set<Integer>> getExtendedRegions(Map<RegionCoord, Set<Integer>> regions) {
		Map<RegionCoord, Set<Integer>> extendedRegions = new HashMap<>();

		for (Map.Entry<RegionCoord, Set<Integer>> regionEntry : regions.entrySet()) {
			RegionCoord regionCoord = regionEntry.getKey();
			Set<Integer> groups = regionEntry.getValue();

			if (isRegionEntryValid(regionCoord, groups)) {
				continue;
			}

			for (int xOffset = -radius; xOffset <= radius; xOffset++) {
				int x = regionCoord.x() + xOffset;
				for (int zOffset = -radius; zOffset <= radius; zOffset++) {
					int z = regionCoord.z() + zOffset;
					RegionCoord extendedRegionCoord = new RegionCoord(regionCoord.worldId(), x, z);
					boolean updateEntry = regionCoord.equals(extendedRegionCoord);

					if (isRegionRadiusValid(extendedRegionCoord, groups, updateEntry)) {
						continue;
					}

					Set<Integer> extendedGroups = extendedRegions.get(extendedRegionCoord);
					if (extendedGroups == null) {
						extendedRegions.put(extendedRegionCoord, groups);
					} else {
						extendedGroups.addAll(groups);
					}
				}
			}
		}

		return extendedRegions;
	}

	private boolean isRegionEntryValid(RegionCoord regionCoord, Set<Integer> groups) {
		RegionData regionData = getRegion(regionCoord, false);
		if (!regionData.isLoaded()) {
			return false;
		}

		Instant enteredPoint = Instant.now().minusMillis(entryRefreshAfterMs);

		synchronized (regionData) {
			for (int groupId : groups) {
				Instant entry = regionData.get(groupId).entry;
				if (entry == null || entry.compareTo(enteredPoint) < 0) {
					return false;
				}
			}
		}

		return true;
	}

	private boolean isRegionRadiusValid(RegionCoord regionCoord, Set<Integer> groups, boolean updateEntry) {
		RegionData regionData = getRegion(regionCoord, false);
		if (!regionData.isLoaded()) {
			return false;
		}

		Instant activePoint = Instant.now().minusMillis(radiusRefreshAfterMs);

		synchronized (regionData) {
			for (int groupId : groups) {
				GroupData groupData = regionData.get(groupId);
				if (groupData.activity == null || groupData.activity.compareTo(activePoint) < 0) {
					return false;
				}
				if (updateEntry) {
					groupData.entry = Instant.now();
				}
			}
		}

		return true;
	}

	private void getUpdatedActivities(List<ActivityItem> activities, RegionCoord regionCoord, Set<Integer> groups, Instant activity) {
		for (int groupId : groups) {
			var activityItem = new ActivityItem(
					regionCoord.worldId(),
					groupId,
					regionCoord.x(),
					regionCoord.z(),
					activity,
					resolution
			);

			activities.add(activityItem);
		}
	}

	private void loadChunkThreadTask(String threadName, AtomicBoolean disabled) {
		this.logger.info("Thread " + threadName + " is started.");

		while (!disabled.get()) {
			try {
				ChunkCoord chunkCoord = chunkLoadQueue.take();

				chunkUnloadQueue.remove(chunkCoord);

				RegionCoord regionCoord = getRegionCoordByChunk(chunkCoord);
				RegionData regionData = getRegion(regionCoord, true);

				synchronized (regionData) {
					regionData.addChunk(chunkCoord);
				}
			} catch (InterruptedException e) {
				if(!disabled.get()) {
					e.printStackTrace();
				}
			}
		}

		this.logger.info("Thread " + threadName + " is stopped.");
	}

	private void unloadChunkTask() {
		ChunkCoord chunkCoord;
		while ((chunkCoord = chunkUnloadQueue.poll()) != null) {
			RegionCoord regionCoord = getRegionCoordByChunk(chunkCoord);
			Map<XZKey, RegionData> worldData = data.get(regionCoord.worldId());
			if (worldData == null) {
				continue;
			}

			XZKey regionKey = new XZKey(regionCoord.x(), regionCoord.z());
			RegionData regionData = worldData.get(regionKey);
			if (regionData == null) {
				continue;
			}

			synchronized (regionData) {
				if (regionData.removeChunk(chunkCoord)) {
					worldData.remove(regionKey);
					unloadCount.incrementAndGet();
				}
			}
		}
	}

	private RegionCoord getRegionCoord(Location location) {
		short worldId = worldIdManager.getInternalWorldId(location.getWorld());
		int regionX = Math.floorDiv(location.getBlockX(), resolution);
		int regionZ = Math.floorDiv(location.getBlockZ(), resolution);

		return new RegionCoord(worldId, regionX, regionZ);
	}

	private RegionCoord getRegionCoordByChunk(ChunkCoord chunkCoord) {
		short worldId = chunkCoord.worldId();
		int regionX = Math.floorDiv(chunkCoord.x() << 4, resolution);
		int regionZ = Math.floorDiv(chunkCoord.z() << 4, resolution);

		return new RegionCoord(worldId, regionX, regionZ);
	}

	private boolean isEnabled(World world) {
		return enabled && world != null && this.worlds.contains(world.getUID());
	}

	public void loadChunk(Chunk chunk) {
		if (!isEnabled(chunk.getWorld())) {
			return;
		}

		short worldId = worldIdManager.getInternalWorldId(chunk.getWorld());
		ChunkCoord chunkCoord = new ChunkCoord(worldId, chunk.getX(), chunk.getZ());

		chunkLoadQueue.add(chunkCoord);
	}

	public void unloadChunk(Chunk chunk) {
		if (!isEnabled(chunk.getWorld())) {
			return;
		}

		short worldId = worldIdManager.getInternalWorldId(chunk.getWorld());
		ChunkCoord chunkCoord = new ChunkCoord(worldId, chunk.getX(), chunk.getZ());
		chunkUnloadQueue.add(chunkCoord);
	}

	public Optional<Instant> getLastActivityTime(Group group, Location location) {
		if (!isEnabled(location.getWorld())) {
			return Optional.of(Instant.ofEpochMilli(group.getActivityTimeStamp()));
		}

		RegionCoord regionCoord = getRegionCoord(location);
		int groupId = group.getGroupId();

		Instant activity = getActivity(regionCoord, groupId);
		if (activity == null) {
			activity = Instant.ofEpochMilli(group.getActivityTimeStamp());
			if (activity.compareTo(defaultActivity) > 0) {
				activity = defaultActivity;
			}
		}

		return Optional.of(activity);
	}

	public void savePlayerActivity(Location from, Location to, Player player) {
		if (!isEnabled(to.getWorld())) {
			return;
		}

		RegionCoord regionFrom = getRegionCoord(from);
		RegionCoord regionTo = getRegionCoord(to);

		if (!regionFrom.equals(regionTo))
			playerUpdates.add(new PlayerUpdate(regionTo, player.getUniqueId()));
	}

	public void savePlayerActivity(Location location, Player player) {
		if (!isEnabled(location.getWorld())) {
			return;
		}

		RegionCoord regionCoord = getRegionCoord(location);
		playerUpdates.add(new PlayerUpdate(regionCoord, player.getUniqueId()));
	}

	public ActivityMapStat getStat() {
		if (!enabled) {
			return null;
		}

		int worlds = data.size();
		int loadedRegions = 0;
		int notLoadedRegions = 0;
		Set<Integer> uniqueGroups = new HashSet<>();
		int loadedActivities = 0;

		for (Map<XZKey, RegionData> world : data.values()) {
			for (RegionData regionData : world.values()) {
				synchronized (regionData) {
					if (regionData.isLoaded()) {
						loadedRegions++;
					} else {
						notLoadedRegions++;
					}

					Set<Integer> groups = regionData.getGroups();

					uniqueGroups.addAll(regionData.getGroups());

					loadedActivities += groups.size();
				}
			}
		}

		var stat = new ActivityMapStat();
		stat.worlds = worlds;
		stat.loadedRegions = loadedRegions;
		stat.notLoadedRegions = notLoadedRegions;
		stat.uniqueGroups = uniqueGroups.size();
		stat.loadedActivities = loadedActivities;
		stat.regionUnloadCount = unloadCount.get();

		timePoll.getStat(stat);

		return stat;
	}
}
