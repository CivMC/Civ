package net.civmc.heliodor.vein;

import net.civmc.heliodor.HeliodorPlugin;
import net.civmc.heliodor.vein.data.Vein;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
            id INT NOT NULL AUTO_INCREMENT,
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
            blocks_available INT NOT NULL,
            discovered BOOL NOT NULL,
            ores INT NOT NULL,
            ores_remaining INT NOT NULL,
            PRIMARY KEY (id))
            """);
    }

    @Override
    public Map<String, Boolean> getSpawnableTypes(Map<String, Integer> spawnFrequencyMinutes, Map<String, Integer> maxSpawns) {
        try (Connection connection = source.getConnection()) {
            ResultSet resultSet = connection.createStatement()
                .executeQuery("SELECT type, MAX(spawned_at) AS max_spawned_at, COUNT(*) AS count FROM veins WHERE ores_remaining >= ores * 0.5 GROUP BY type");

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
    public List<Vein> getVeins() {
        try (Connection connection = source.getConnection()) {
            ResultSet resultSet = connection.createStatement()
                .executeQuery("SELECT * FROM veins WHERE ores_remaining > 0");

            List<Vein> veins = new ArrayList<>();
            while (resultSet.next()) {
                veins.add(new Vein(
                    resultSet.getInt("id"),
                    resultSet.getString("type"),
                    resultSet.getTimestamp("spawned_at").getTime(),
                    resultSet.getString("world"),
                    resultSet.getInt("radius"),
                    resultSet.getInt("x"),
                    resultSet.getInt("y"),
                    resultSet.getInt("z"),
                    resultSet.getInt("offset_x"),
                    resultSet.getInt("offset_y"),
                    resultSet.getInt("offset_z"),
                    resultSet.getInt("blocks_available"),
                    resultSet.getBoolean("discovered"),
                    resultSet.getInt("ores"),
                    resultSet.getInt("ores_remaining")
                ));
            }

            return veins;
        } catch (SQLException ex) {
            JavaPlugin.getPlugin(HeliodorPlugin.class).getLogger().log(Level.WARNING, "Getting veins", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public int addVein(Vein vein) {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO veins (type, spawned_at, world, radius, x, y, z, offset_x, offset_y, offset_z, blocks_available, discovered, ores, ores_remaining) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, vein.type());
            statement.setTimestamp(2, new Timestamp(vein.spawnedAt()));
            statement.setString(3, vein.world());
            statement.setInt(4, vein.radius());
            statement.setInt(5, vein.x());
            statement.setInt(6, vein.y());
            statement.setInt(7, vein.z());
            statement.setInt(8, vein.offsetX());
            statement.setInt(9, vein.offsetY());
            statement.setInt(10, vein.offsetZ());
            statement.setInt(11, vein.blocksAvailable());
            statement.setBoolean(12, vein.discovered());
            statement.setInt(13, vein.ores());
            statement.setInt(14, vein.oresRemaining());

            if (statement.executeUpdate() != 1) {
                return -1;
            }

            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
            return -1;
        } catch (SQLException ex) {
            JavaPlugin.getPlugin(HeliodorPlugin.class).getLogger().log(Level.WARNING, "Adding vein", ex);
            return -1;
        }
    }

    @Override
    public boolean updateVein(int veinId, Integer blocksMined, Integer oresMined, boolean discovered) {
        if (blocksMined == null && oresMined == null && !discovered) {
            return true;
        }
        try (Connection connection = source.getConnection()) {
            List<String> updates = new ArrayList<>();
            if (blocksMined != null) {
                updates.add("blocks_available = blocks_available - ?");
            }
            if (oresMined != null) {
                updates.add("ores_remaining = ores_remaining - ?");
            }
            if (discovered) {
                updates.add("discovered = TRUE");
            }
            PreparedStatement statement = connection.prepareStatement("UPDATE veins SET " + String.join(", ", updates) + " WHERE id = ?");
            int index = 1;
            if (blocksMined != null) {
                statement.setInt(index++, blocksMined);
            }
            if (oresMined != null) {
                statement.setInt(index++, oresMined);
            }

            statement.setInt(index, veinId);

            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            JavaPlugin.getPlugin(HeliodorPlugin.class).getLogger().log(Level.WARNING, "Adding vein", ex);
            return false;
        }
    }
}
