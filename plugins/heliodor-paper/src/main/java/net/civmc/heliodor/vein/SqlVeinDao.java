package net.civmc.heliodor.vein;

import net.civmc.heliodor.HeliodorPlugin;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class SqlVeinDao implements VeinDao {

    private final ManagedDatasource source;

    public SqlVeinDao(ManagedDatasource source) {
        this.source = source;
    }

    public void registerMigrations() {
        source.registerMigration(2, false, """
            CREATE TABLE IF NOT EXISTS veins (
            type VARCHAR(64) NOT NULL,
            spawned_at TIMESTAMP NOT NULL,
            world VARCHAR(64) NOT NULL,
            radius INT NOT NULL,
            x INT NOT NULL,
            y INT NOT NULL,
            z INT NOT NULL,
            offset_x INT NOT NULL,
            offset_y INT NOT NULL,
            offset_z INT NOT NULL,
            blocks_available_estimate INT NOT NULL,
            blocks_mined INT NOT NULL,
            discovered BOOL NOT NULL,
            ores INT NOT NULL)
            """);
    }

    @Override
    public Map<String, Boolean> getSpawnableTypes(Map<String, Integer> spawnFrequencyMinutes, Map<String, Integer> maxSpawns) {
        try (Connection connection = source.getConnection()) {
            ResultSet resultSet = connection.createStatement()
                .executeQuery("SELECT type, MAX(spawned_at) AS max_spawned_at, COUNT(*) AS count FROM veins GROUP BY type");

            Map<String, Boolean> possibleTypes = new HashMap<>();
            while (resultSet.next()) {
                String type = resultSet.getString("type");
                int count = resultSet.getInt("count");
                long maxSpawnedAtMillis = resultSet.getTimestamp("max_spawned_at").getTime();

                if (maxSpawns.containsKey(type) && count >= maxSpawns.get(type)) {
                    possibleTypes.put(type, false);
                    continue;
                }
                if (spawnFrequencyMinutes.containsKey(type) && maxSpawnedAtMillis + TimeUnit.MINUTES.toMillis(spawnFrequencyMinutes.get(type)) > System.currentTimeMillis()) {
                    possibleTypes.put(type, false);
                    continue;
                }

                possibleTypes.put(type, true);
            }

            return possibleTypes;
        } catch (SQLException ex) {
            JavaPlugin.getPlugin(HeliodorPlugin.class).getLogger().log(Level.WARNING, "Getting spawnable types", ex);
            return Collections.emptyMap();
        }
    }

    @Override
    public boolean addVein(Vein vein) {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO veins (type, spawned_at, world, radius, x, y, z, offset_x, offset_y, offset_z, blocks_available_estimate, blocks_mined, discovered, ores) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            statement.setString(1, vein.type());
            statement.setLong(2, vein.spawnedAt());
            statement.setString(3, vein.world());
            statement.setInt(4, vein.radius());
            statement.setInt(5, vein.x());
            statement.setInt(6, vein.y());
            statement.setInt(7, vein.z());
            statement.setInt(8, vein.offsetX());
            statement.setInt(9, vein.offsetY());
            statement.setInt(10, vein.offsetZ());
            statement.setInt(11, vein.blocksAvailableEstimate());
            statement.setInt(12, vein.blocksMined());
            statement.setBoolean(13, vein.discovered());
            statement.setInt(14, vein.ores());

            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            JavaPlugin.getPlugin(HeliodorPlugin.class).getLogger().log(Level.WARNING, "Adding vein", ex);
            return false;
        }
    }
}
