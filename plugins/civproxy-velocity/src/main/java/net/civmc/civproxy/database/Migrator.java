package net.civmc.civproxy.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

public class Migrator {

    private final Map<String, NavigableMap<Integer, String[]>> migrations = new HashMap<>();

    public void registerMigration(String namespace, int id, String... sql) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(sql);
        if (namespace.length() > 64) {
            throw new IllegalArgumentException("namespace must not be longer than 64 characters");
        }
        if (id < 0) {
            throw new IllegalArgumentException("migration id must be at least 0");
        }
        boolean present = this.migrations.computeIfAbsent(namespace, k -> new TreeMap<>()).putIfAbsent(id, sql) != null;
        if (present) {
            throw new IllegalStateException("Migration already exists with namespace " + namespace + " and ID " + id);
        }
    }

    public void migrate(Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS migrations (" +
            "namespace VARCHAR(64) PRIMARY KEY," +
            "id INT NOT NULL)");

        for (Map.Entry<String, NavigableMap<Integer, String[]>> entry : migrations.entrySet()) {
            PreparedStatement getMigrationId = connection.prepareStatement("SELECT id FROM migrations WHERE namespace = ? FOR UPDATE");
            getMigrationId.setString(1, entry.getKey());
            ResultSet resultSet = getMigrationId.executeQuery();
            int minId;
            if (resultSet.next()) {
                minId = resultSet.getInt("id");
            } else {
                minId = -1;
            }

            NavigableMap<Integer, String[]> value = entry.getValue().tailMap(minId, false);
            int maxId = entry.getValue().lastKey();
            for (String[] migration : value.sequencedValues()) {
                for (String sql : migration) {
                    connection.createStatement().executeUpdate(sql);
                }
            }

            if (maxId != minId) {
                PreparedStatement setMigrationId = connection.prepareStatement("REPLACE INTO migations (namespace, id) VALUES (?, ?)");
                setMigrationId.setString(1, entry.getKey());
                setMigrationId.setInt(2, maxId);
            }
        }
        connection.setAutoCommit(true);
    }
}
