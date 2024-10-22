package net.civmc.kitpvp.arena.data;

import net.civmc.kitpvp.KitPvpPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class SqlArenaDao implements ArenaDao {

    private final ManagedDatasource source;

    public SqlArenaDao(ManagedDatasource source) {
        this.source = source;
        source.registerMigration(2, false,
            """
                CREATE TABLE IF NOT EXISTS arenas (
                    name VARCHAR(64),
                    spawn_x DOUBLE NOT NULL,
                    spawn_y DOUBLE NOT NULL,
                    spawn_z DOUBLE NOT NULL,
                    spawn_yaw FLOAT NOT NULL,
                    icon VARCHAR(64),
                    PRIMARY KEY (name)
                )
                """);
        source.registerMigration(5, false,
            """
                ALTER TABLE arenas ADD COLUMN IF NOT EXISTS display_name VARCHAR(64)
                """);
    }

    @Override
    public List<Arena> getArenas() {
        try (Connection connection = source.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM arenas");
            List<Arena> arenas = new ArrayList<>();
            while (resultSet.next()) {
                arenas.add(new Arena(
                    resultSet.getString("name"),
                    resultSet.getString("display_name"),
                    new Location(null, resultSet.getDouble("spawn_x"), resultSet.getDouble("spawn_y"), resultSet.getDouble("spawn_z"), resultSet.getFloat("spawn_yaw"), 0),
                    Material.valueOf(resultSet.getString("icon"))
                ));
            }
            return arenas;
        } catch (SQLException ex) {
            JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Error listing arenas", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean newArena(Arena arena) {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT IGNORE INTO arenas (name, spawn_x, spawn_y, spawn_z, spawn_yaw, icon) VALUES (?, ?, ?, ?, ?, ?)");
            statement.setString(1, arena.name());
            statement.setDouble(2, arena.spawn().getX());
            statement.setDouble(3, arena.spawn().getY());
            statement.setDouble(4, arena.spawn().getZ());
            statement.setFloat(5, arena.spawn().getYaw());
            statement.setString(6, arena.icon().toString());

            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Error creating new arena", ex);
            return false;
        }
    }

    @Override
    public boolean deleteArena(String arenaName) {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM arenas WHERE name = ?");
            statement.setString(1, arenaName);

            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Error deleting arena", ex);
            return false;
        }
    }

    @Override
    public boolean setDisplayName(String arenaName, String displayName) {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE arenas SET display_name = ? WHERE name = ?");
            statement.setString(1, displayName);
            statement.setString(2, arenaName);

            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Error setting display name of arena", ex);
            return false;
        }
    }
}
