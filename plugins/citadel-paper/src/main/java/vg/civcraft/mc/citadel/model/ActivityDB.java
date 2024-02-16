package vg.civcraft.mc.citadel.model;

import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

public class ActivityDB {
	private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ctdl_activity_map ("
			+ "group_id INT, "
			+ "world SMALLINT, "
			+ "x INT, "
			+ "z INT, "
			+ "resolution SMALLINT UNSIGNED, "
			+ "activity TIMESTAMP, "
			+ "PRIMARY KEY(group_id, world, x, z, resolution))";

	private static final String EXISTS_INDEX = "SELECT * FROM information_schema.statistics WHERE table_name = 'ctdl_activity_map' AND table_schema = database() AND index_name = 'PRIMARY' AND seq_in_index = 1 AND column_name = 'resolution' LIMIT 0, 1";
	private static final String CREATE_INDEX = "ALTER TABLE ctdl_activity_map DROP PRIMARY KEY, ADD PRIMARY KEY (resolution, world, x, z, group_id)";
	private static final String GET_REGION_ACTIVITIES = "SELECT group_id, activity FROM ctdl_activity_map WHERE world = ? AND x = ? AND z = ? AND resolution = ?";
	private static final String UPDATE_ACTIVITY = "INSERT INTO ctdl_activity_map (group_id, world, x, z, resolution, activity) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE activity = ?";

	private final ManagedDatasource datasource;

	public ActivityDB(ManagedDatasource datasource) {
		this.datasource = datasource;
	}

	public boolean enable() {
		try (Connection connection = datasource.getConnection()) {
			connection.createStatement().executeUpdate(CREATE_TABLE);

			boolean indexExists = false;

			try (PreparedStatement statement = connection.prepareStatement(EXISTS_INDEX)) {
				try (ResultSet resultSet = statement.executeQuery()) {
					while (resultSet.next()) {
						indexExists = true;
					}
				}
			}

			if (!indexExists) {
				connection.createStatement().executeUpdate(CREATE_INDEX);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	public void update(List<ActivityItem> activities) {
		try (Connection connection = datasource.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(UPDATE_ACTIVITY)) {
				for (ActivityItem activityItem : activities) {
					statement.setInt(1, activityItem.group());
					statement.setShort(2, activityItem.world());
					statement.setInt(3, activityItem.x());
					statement.setInt(4, activityItem.z());
					statement.setInt(5, activityItem.resolution());
					statement.setTimestamp(6, Timestamp.from(activityItem.activity()));
					statement.setTimestamp(7, Timestamp.from(activityItem.activity()));
					statement.addBatch();
				}

				statement.executeBatch();
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public void select(short worldId, int x, int z, int resolution, Consumer<ActivityItem> process) {
		try (Connection connection = datasource.getConnection()) {
			connection.setAutoCommit(false);
			try (PreparedStatement statement = connection.prepareStatement(GET_REGION_ACTIVITIES, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
				statement.setFetchSize(Integer.MIN_VALUE);
				statement.setShort(1, worldId);
				statement.setInt(2, x);
				statement.setInt(3, z);
				statement.setInt(4, resolution);

				try (ResultSet resultSet = statement.executeQuery()) {
					while (resultSet.next()) {
						int groupId = resultSet.getShort("group_id");
						Instant activity = resultSet.getTimestamp("activity").toInstant();

						var activityItem = new ActivityItem(worldId, groupId, x, z, activity, resolution);

						process.accept(activityItem);
					}
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
}
