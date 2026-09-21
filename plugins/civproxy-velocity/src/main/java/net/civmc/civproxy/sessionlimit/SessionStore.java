package net.civmc.civproxy.sessionlimit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import net.civmc.nameapi.Migrator;
import org.slf4j.Logger;

/**
 * Keeps streaks across proxy restarts. A player who was on the server when the proxy went down is treated as having
 * left at the last save.
 */
public final class SessionStore {

    private static final String UPSERT = "INSERT INTO session_limit_streaks "
        + "(player_uuid, played_ms, left_at_ms, grace_ends_at_ms, saved_at_ms) VALUES (?, ?, ?, ?, ?) "
        + "ON DUPLICATE KEY UPDATE played_ms = VALUES(played_ms), left_at_ms = VALUES(left_at_ms), "
        + "grace_ends_at_ms = VALUES(grace_ends_at_ms), saved_at_ms = VALUES(saved_at_ms)";

    private final Logger logger;
    private final DataSource dataSource;

    public SessionStore(final Logger logger, final DataSource dataSource) {
        this.logger = logger;
        this.dataSource = dataSource;
    }

    /**
     * @return false if the database isn't usable, in which case streaks are kept in memory only
     */
    public boolean migrate() {
        final Migrator migrator = new Migrator();
        migrator.registerMigration("civproxy_session_limit", 0,
            "CREATE TABLE IF NOT EXISTS session_limit_streaks ("
                + "player_uuid CHAR(36) NOT NULL PRIMARY KEY,"
                + "played_ms BIGINT NOT NULL,"
                + "left_at_ms BIGINT NULL,"
                + "grace_ends_at_ms BIGINT NULL,"
                + "saved_at_ms BIGINT NOT NULL,"
                + "INDEX session_limit_streaks_saved_at (saved_at_ms))");
        try (Connection connection = this.dataSource.getConnection()) {
            migrator.migrate(connection);
            return true;
        } catch (final SQLException exception) {
            this.logger.error("Session limit could not set up its table; streaks will not survive a proxy restart", exception);
            return false;
        }
    }

    public Map<UUID, PlayStreak> loadSince(final Instant cutoff) {
        final Map<UUID, PlayStreak> streaks = new HashMap<>();
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT player_uuid, played_ms, left_at_ms, grace_ends_at_ms, saved_at_ms "
                     + "FROM session_limit_streaks WHERE saved_at_ms >= ?")) {
            statement.setLong(1, cutoff.toEpochMilli());
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    final long leftAt = results.getLong("left_at_ms");
                    final Instant left = results.wasNull()
                        ? Instant.ofEpochMilli(results.getLong("saved_at_ms"))
                        : Instant.ofEpochMilli(leftAt);
                    final long graceEndsAt = results.getLong("grace_ends_at_ms");
                    final Instant grace = results.wasNull() ? null : Instant.ofEpochMilli(graceEndsAt);
                    streaks.put(UUID.fromString(results.getString("player_uuid")),
                        new PlayStreak(Duration.ofMillis(results.getLong("played_ms")), left, grace));
                }
            }
        } catch (final SQLException | IllegalArgumentException exception) {
            this.logger.error("Session limit could not load saved streaks", exception);
        }
        return streaks;
    }

    public void save(final Map<UUID, PlayStreak.Snapshot> snapshots, final Instant now) {
        if (snapshots.isEmpty()) {
            return;
        }
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPSERT)) {
            for (final Map.Entry<UUID, PlayStreak.Snapshot> entry : snapshots.entrySet()) {
                final PlayStreak.Snapshot snapshot = entry.getValue();
                statement.setString(1, entry.getKey().toString());
                statement.setLong(2, snapshot.played().toMillis());
                setInstant(statement, 3, snapshot.onServer() ? null : snapshot.leftAt());
                setInstant(statement, 4, snapshot.graceEndsAt());
                statement.setLong(5, now.toEpochMilli());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (final SQLException exception) {
            this.logger.error("Session limit could not save streaks", exception);
        }
    }

    public void deleteSavedBefore(final Instant cutoff) {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "DELETE FROM session_limit_streaks WHERE saved_at_ms < ?")) {
            statement.setLong(1, cutoff.toEpochMilli());
            statement.executeUpdate();
        } catch (final SQLException exception) {
            this.logger.error("Session limit could not prune old streaks", exception);
        }
    }

    private static void setInstant(final PreparedStatement statement, final int index, final Instant instant) throws SQLException {
        if (instant == null) {
            statement.setNull(index, Types.BIGINT);
        } else {
            statement.setLong(index, instant.toEpochMilli());
        }
    }
}
