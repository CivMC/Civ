package net.civmc.kitpvp.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.civmc.kitpvp.kit.Kit;
import net.civmc.kitpvp.kit.KitPvpDao;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class SqlKitPvpDao implements KitPvpDao {

    private final ManagedDatasource source;

    public SqlKitPvpDao(ManagedDatasource source) {
        this.source = source;
        source.registerMigration(1, false,
            """
                CREATE TABLE IF NOT EXISTS kits (
                    id INT AUTO_INCREMENT,
                    name VARCHAR(64) NOT NULL,
                    player VARCHAR(36),
                    icon BLOB NOT NULL,
                    inventory BLOB NOT NULL,
                    public BOOL NOT NULL,
                    PRIMARY KEY (id),
                    UNIQUE KEY (name, player)
                )
                """);
    }

    private Kit getKit(ResultSet resultSet) throws SQLException {
        return new Kit(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getBoolean("public"),
            ItemStack.deserializeBytes(resultSet.getBytes("icon")).getType(),
            ItemStack.deserializeItemsFromBytes(resultSet.getBytes("inventory")));
    }

    private Kit queryKit(Connection connection, int id) throws SQLException {
        PreparedStatement query = connection.prepareStatement("SELECT id, name, public, icon, inventory FROM kits WHERE id = ?");
        query.setInt(1, id);

        ResultSet resultSet = query.executeQuery();
        if (!resultSet.next()) {
            return null;
        }
        return getKit(resultSet);
    }

    @Override
    public Kit getKit(String name, UUID player) {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT id, name, public, icon, inventory FROM kits WHERE ((? IS NULL AND public) OR (player = ?)) AND name = ?");
            statement.setString(1, player == null ? null : player.toString());
            statement.setString(2, player == null ? null : player.toString());
            statement.setString(3, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getKit(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Kit getKit(int id) {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT id, name, public, icon, inventory FROM kits WHERE id = ?");
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getKit(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Kit> getKits(UUID player) {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT id, name, public, icon, inventory FROM kits WHERE (player = ? AND NOT public) OR public");
            statement.setString(1, player.toString());
            ResultSet resultSet = statement.executeQuery();
            List<Kit> kits = new ArrayList<>();
            while (resultSet.next()) {
                kits.add(getKit(resultSet));
            }
            return kits;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Kit createKit(String name, UUID player) {
        if (name.length() > 64 || !name.matches("^[A-z0-9_-]+$")) {
            return null;
        }
        try (Connection connection = source.getConnection()) {
            connection.setAutoCommit(false);
            PreparedStatement statement = connection.prepareStatement("INSERT INTO kits (name, player, icon, inventory, public) VALUES (?, ?, ?, ?, false) ON DUPLICATE KEY UPDATE id = id", PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, name);
            statement.setString(2, player.toString());
            statement.setBytes(3, new ItemStack(Material.IRON_SWORD).serializeAsBytes());
            statement.setBytes(4, ItemStack.serializeItemsAsBytes(new ItemStack[41]));

            int rows = statement.executeUpdate();
            if (rows == 0) {
                return null;
            }

            int id;
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1);
                } else {
                    return null;
                }
            }

            Kit kit = queryKit(connection, id);
            connection.commit();
            return kit;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Kit setPublicKit(int id, boolean isPublic) {
        try (Connection connection = source.getConnection()) {
            connection.setAutoCommit(false);
            PreparedStatement update = connection.prepareStatement("UPDATE kits SET public = ? WHERE id = ?");
            update.setBoolean(1, isPublic);
            update.setInt(2, id);

            update.executeUpdate();

            Kit kit = queryKit(connection, id);
            connection.commit();
            return kit;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Kit updateKit(int id, Material icon, ItemStack[] items) {
        try (Connection connection = source.getConnection()) {
            connection.setAutoCommit(false);
            PreparedStatement statement = connection.prepareStatement("UPDATE kits SET icon = ?, inventory = ? WHERE id = ?");
            statement.setBytes(1, new ItemStack(icon).serializeAsBytes());
            statement.setBytes(2, ItemStack.serializeItemsAsBytes(items));
            statement.setInt(3, id);

            statement.executeUpdate();

            Kit kit = queryKit(connection, id);
            connection.commit();
            return kit;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Kit renameKit(int id, String name) {
        if (name.length() > 64 || !name.matches("^[A-z0-9_-]+$")) {
            return null;
        }
        try (Connection connection = source.getConnection()) {
            connection.setAutoCommit(false);
            PreparedStatement update = connection.prepareStatement("UPDATE IGNORE kits SET name = ? WHERE id = ?");
            update.setString(1, name);
            update.setInt(2, id);

            if (update.executeUpdate() == 0) {
                return null;
            }

            Kit kit = queryKit(connection, id);
            connection.commit();
            return kit;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteKit(int id) {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM kits WHERE id = ?");
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
