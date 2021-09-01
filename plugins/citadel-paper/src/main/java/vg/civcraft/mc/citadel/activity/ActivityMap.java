package vg.civcraft.mc.citadel.activity;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.locations.global.WorldIDManager;
import vg.civcraft.mc.namelayer.group.Group;

public class ActivityMap {

	private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ctdl_activity_map ("
			+ "group_id INT, "
			+ "world SMALLINT, "
			+ "x INT, "
			+ "z INT, "
			+ "resolution INT, "
			+ "activity TIMESTAMP, "
			+ "PRIMARY KEY(group_id, world, x, z, resolution))";
	private static final String GET_ACTIVITY = "SELECT x, z, activity FROM ctdl_activity_map WHERE group_id = ? AND world = ? AND resolution = ?";
	private static final String UPDATE_ACTIVITY = "INSERT INTO ctdl_activity_map (group_id, world, x, z, resolution, activity) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE activity = ?";

	private final WorldIDManager worldIdManager;
	// Must be array list so we can resize it
	private final Map<Short, LoadingCache<Integer, Table<Integer, Integer, Instant>>> activityTimes;
	private final Queue<Update> updates = new LinkedBlockingQueue<>();

	private final List<String> worlds;
	private final int resolution;
	private final int radius;
	private final Instant defaultActivity;

	private final ManagedDatasource source;

	public ActivityMap(ManagedDatasource source) {
		this.source = source;
		worldIdManager = CivModCorePlugin.getInstance().getWorldIdManager();
		activityTimes = new ConcurrentHashMap<>();

		resolution = Citadel.getInstance().getConfigManager().getActivityMapResolution();
		radius = Citadel.getInstance().getConfigManager().getActivityMapRadius();
		worlds = Citadel.getInstance().getConfigManager().getActivityWorlds();
		defaultActivity = Instant
				.ofEpochSecond(Citadel.getInstance().getConfigManager().getActivityDefault());

		Bukkit.getScheduler().runTaskTimerAsynchronously(Citadel.getInstance(), () -> {
			try (Connection connection = source.getConnection()) {
				PreparedStatement statement = connection.prepareStatement(UPDATE_ACTIVITY);

				Update update;
				while ((update = updates.poll()) != null) {
					statement.setInt(1, update.group);
					statement.setShort(2, update.world);
					statement.setInt(3, update.x);
					statement.setInt(4, update.z);
					statement.setInt(5, resolution);
					statement.setTimestamp(6, Timestamp.from(update.timestamp));
					statement.setTimestamp(7, Timestamp.from(update.timestamp));
					statement.addBatch();
				}

				statement.executeBatch();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			//}, 20 * 60 * 4, 20 * 60 * 4);
		}, 20 * 30, 20 * 30);

		try (Connection connection = source.getConnection()) {
			connection.createStatement().executeUpdate(CREATE_TABLE);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	private LoadingCache<Integer, Table<Integer, Integer, Instant>> getCache(World world) {
		return activityTimes.computeIfAbsent(worldIdManager.getInternalWorldId(world), id ->
				CacheBuilder.newBuilder()
						.maximumWeight(200000)
						.weigher((Integer k, Table<Integer, Integer, Instant> v) -> v.size())
						.build(new CacheLoader<>() {
							@Override
							public Table<Integer, Integer, Instant> load(Integer groupId) throws SQLException {
								Table<Integer, Integer, Instant> list = HashBasedTable.create();
								try (Connection connection = source.getConnection()) {
									PreparedStatement statement = connection
											.prepareStatement(GET_ACTIVITY);
									statement.setInt(1, groupId);
									statement.setShort(2, id);
									statement.setInt(3, resolution);
									ResultSet set = statement.executeQuery();
									while (set.next()) {
										int scaledX = set.getInt("x");
										int scaledZ = set.getInt("z");
										list.put(scaledX, scaledZ, set.getTimestamp("activity").toInstant());
									}
								}
								return list;
							}
						})
		);
	}

	public void update(World world, Iterable<Integer> groups, int scaledX, int scaledZ) {
		var cache = getCache(world);
		for (int i = -radius; i <= radius; i++) {
			for (int j = -radius; j <= radius; j++) {
				try {
					for (int group : groups) {
						Table<Integer, Integer, Instant> list = cache.get(group);
						int nx = scaledX + i;
						int nz = scaledZ + j;
						Instant now = Instant.now();
						synchronized (list) {
							list.put(nx, nz, now);
						}

						this.updates.add(new Update(worldIdManager.getInternalWorldId(world),
								group, nx, nz, now));
					}
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Optional<Instant> getLastActivityTime(Group group, Location location) {
		if (!isEnabled(location.getWorld())) {
			return Optional.of(Instant.ofEpochMilli(group.getActivityTimeStamp()));
		}

		try {
			Table<Integer, Integer, Instant> activities = getCache(location.getWorld()).get(group.getGroupId());

			int scaledX = location.getBlockX() / resolution;
			int scaledZ = location.getBlockZ() / resolution;

			synchronized (activities) {
				Instant get = activities.get(scaledX, scaledZ);
				if (get == null) {
					Instant activity = Instant.ofEpochMilli(group.getActivityTimeStamp());
					if (activity.compareTo(defaultActivity) <= 0) {
						// If the group was last active before activity map was added, use the group's activity time for decay
						get = activity;
					} else {
						// If the group has been active since, use the activity map decay
						get = defaultActivity;
					}
				}
				return Optional.of(get);
			}
		} catch (ExecutionException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	private static class Update {

		private final short world;
		private final int group;
		private final int x;
		private final int z;
		private final Instant timestamp;

		private Update(short world, int group, int x, int z, Instant timestamp) {
			this.world = world;
			this.group = group;
			this.x = x;
			this.z = z;
			this.timestamp = timestamp;
		}
	}

	public boolean isEnabled(World world) {
		return world != null && this.worlds.contains(world.getName());
	}
}
