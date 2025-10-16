package sh.okx.railswitch.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.block.Block;
import sh.okx.railswitch.RailSwitchPlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

/**
 * Handles persisting detector rail switch data.
 */
public final class RailSwitchStorage {

    private static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS railswitch_switches (
            world_uuid VARCHAR(36) NOT NULL,
            x INT NOT NULL,
            y INT NOT NULL,
            z INT NOT NULL,
            header VARCHAR(64) NOT NULL,
            `lines` TEXT NOT NULL,
            PRIMARY KEY (world_uuid, x, y, z)
        )
        """;
    private static final String SELECT_ALL = """
        SELECT world_uuid, x, y, z, header, `lines`
        FROM railswitch_switches
        """;
    private static final String UPSERT_STATEMENT = """
        INSERT INTO railswitch_switches (world_uuid, x, y, z, header, `lines`)
        VALUES (?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
            header = VALUES(header),
            `lines` = VALUES(`lines`)
        """;
    private static final String DELETE_STATEMENT = """
        DELETE FROM railswitch_switches
        WHERE world_uuid = ? AND x = ? AND y = ? AND z = ?
        """;

    private final RailSwitchPlugin plugin;
    private final ManagedDatasource datasource;
    private final Map<RailSwitchKey, RailSwitchRecord> entries;

    public RailSwitchStorage(RailSwitchPlugin plugin, ManagedDatasource datasource) {
        this.plugin = plugin;
        this.datasource = datasource;
        this.entries = new HashMap<>();
        registerMigrations();
    }

    public void load() {
        entries.clear();
        loadFromDatabase();
    }

    public void save() {
        // Database storage writes through immediately; nothing to do here.
    }

    public Optional<RailSwitchRecord> get(Block block) {
        if (block == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(entries.get(RailSwitchKey.from(block)));
    }

    public Optional<RailSwitchRecord> get(RailSwitchKey key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(entries.get(key));
    }

    public RailSwitchRecord upsert(Block block, String header, Collection<String> lines) {
        RailSwitchKey key = RailSwitchKey.from(block);
        return upsert(key, header, lines);
    }

    public RailSwitchRecord upsert(RailSwitchKey key, String header, Collection<String> lines) {
        RailSwitchRecord record = new RailSwitchRecord(key, header,
            lines == null ? Collections.emptyList() : List.copyOf(lines));
        entries.put(key, record);
        persistRecord(record);
        return record;
    }

    public Optional<RailSwitchRecord> remove(Block block) {
        if (block == null) {
            return Optional.empty();
        }
        return remove(RailSwitchKey.from(block));
    }

    public Optional<RailSwitchRecord> remove(RailSwitchKey key) {
        if (key == null) {
            return Optional.empty();
        }
        RailSwitchRecord removed = entries.remove(key);
        if (removed != null) {
            deleteRecord(key);
        }
        return Optional.ofNullable(removed);
    }

    public Collection<RailSwitchRecord> values() {
        return Collections.unmodifiableCollection(entries.values());
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    private void registerMigrations() {
        datasource.registerMigration(1, false, CREATE_TABLE);
    }

    private void loadFromDatabase() {
        try (Connection connection = datasource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                String worldIdRaw = results.getString("world_uuid");
                UUID worldId;
                try {
                    worldId = UUID.fromString(worldIdRaw);
                } catch (IllegalArgumentException exception) {
                    plugin.getLogger().warning("Skipping rail switch with invalid world id: " + worldIdRaw);
                    continue;
                }
                int x = results.getInt("x");
                int y = results.getInt("y");
                int z = results.getInt("z");
                String header = results.getString("header");
                if (header == null) {
                    header = "";
                }
                String linesRaw = results.getString("lines");
                List<String> lines = decodeLines(linesRaw);
                RailSwitchKey key = RailSwitchKey.of(worldId, x, y, z);
                RailSwitchRecord record = new RailSwitchRecord(key, header, lines);
                entries.put(key, record);
            }
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load rail switch data from database.", exception);
        }
    }

    private void persistRecord(RailSwitchRecord record) {
        try (Connection connection = datasource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPSERT_STATEMENT)) {
            statement.setString(1, record.getWorldId().toString());
            statement.setInt(2, record.getX());
            statement.setInt(3, record.getY());
            statement.setInt(4, record.getZ());
            statement.setString(5, record.getHeader() == null ? "" : record.getHeader());
            statement.setString(6, encodeLines(record.getLines()));
            statement.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to store rail switch data for " + record.toKey(), exception);
        }
    }

    private void deleteRecord(RailSwitchKey key) {
        try (Connection connection = datasource.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_STATEMENT)) {
            statement.setString(1, key.getWorldId().toString());
            statement.setInt(2, key.getX());
            statement.setInt(3, key.getY());
            statement.setInt(4, key.getZ());
            statement.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete rail switch data for " + key, exception);
        }
    }

    private List<String> decodeLines(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return List.copyOf(Arrays.asList(value.split("\n", -1)));
    }

    private String encodeLines(Collection<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }
        return String.join("\n", lines);
    }
}
