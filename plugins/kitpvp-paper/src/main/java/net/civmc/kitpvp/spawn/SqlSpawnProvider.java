package net.civmc.kitpvp.spawn;

import net.civmc.kitpvp.KitPvpPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class SqlSpawnProvider implements SpawnProvider {

    private final ManagedDatasource source;
    private Location spawn;

    public SqlSpawnProvider(ManagedDatasource source) {
        this.source = source;
        source.registerMigration(3, false, """
            CREATE TABLE IF NOT EXISTS spawn (
                id INT PRIMARY KEY,
                world VARCHAR(64) NOT NULL,
                x DOUBLE NOT NULL,
                y DOUBLE NOT NULL,
                z DOUBLE NOT NULL,
                yaw FLOAT NOT NULL
            )
            """);

        Bukkit.getScheduler().scheduleSyncDelayedTask(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
            try {
                loadSpawn();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void loadSpawn() throws SQLException {
        try (Connection connection = source.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT world, x, y, z, yaw FROM spawn WHERE id = 1");
            if (resultSet.next()) {
                this.spawn = new Location(
                    Bukkit.getWorld(resultSet.getString("world")),
                    resultSet.getDouble("x"),
                    resultSet.getDouble("y"),
                    resultSet.getDouble("z"),
                    resultSet.getFloat("yaw"),
                    0
                );
            }
        }
    }


    @Override
    public boolean setSpawn(Location location) {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("REPLACE INTO spawn (id, world, x, y, z, yaw) VALUES (1, ?, ?, ?, ?, ?)");
            statement.setString(1, location.getWorld().getName());
            statement.setDouble(2, location.getX());
            statement.setDouble(3, location.getY());
            statement.setDouble(4, location.getZ());
            statement.setFloat(5, location.getYaw());

            boolean success = statement.executeUpdate() > 0;
            if (success) {
                this.spawn = location;
            }
            return success;
        } catch (SQLException e) {
            JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Cannot set spawn", e);
            return false;
        }
    }

    @Override
    public Location getSpawn() {
        return spawn;
    }
}
