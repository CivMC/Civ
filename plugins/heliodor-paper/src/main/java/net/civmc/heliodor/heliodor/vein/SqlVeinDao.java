package net.civmc.heliodor.heliodor.vein;

import net.civmc.heliodor.HeliodorPlugin;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

import java.sql.Connection;
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
            ores INT NOT NULL,
            INDEX chunk (chunk_x, chunk_z, world_id),
            INDEX pos (x_offset, y, z_offset, world_id),
            CONSTRAINT loc UNIQUE (chunk_x,chunk_z,x_offset,y,z_offset,world_id))
            """);
    }

    @Override
    public Map<String, Boolean> getSpawnableTypes(Map<String, Integer> spawnFrequencyMinutes, Map<String, Integer> maxSpawns) {
        try (Connection connection = source.getConnection()) {
            // Could use HAVING here and do all the filtering in SQL but that would require complicated SQL builders
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
}
