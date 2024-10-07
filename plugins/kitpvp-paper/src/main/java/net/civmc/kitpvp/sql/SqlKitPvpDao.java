package net.civmc.kitpvp.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.civmc.kitpvp.dao.Kit;
import net.civmc.kitpvp.dao.KitPvpDao;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class SqlKitPvpDao implements KitPvpDao {

    private final ManagedDatasource source;

    public SqlKitPvpDao(ManagedDatasource source) {
        this.source = source;
        registerMigrations();
        source.updateDatabase();
    }

    private void registerMigrations() {
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
                """, """
                CREATE TABLE IF NOT EXISTS deleted_kits (
                    id INT NOT NULL,
                    name VARCHAR(64) NOT NULL,
                    player VARCHAR(36),
                    icon BLOB NOT NULL,
                    inventory BLOB NOT NULL,
                    public BOOL NOT NULL,
                    PRIMARY KEY (id)
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
    public void newKit(String name, UUID player) {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO kits (name, player, icon, inventory, public) VALUES (?, ?, ?, ?, false) ON DUPLICATE KEY UPDATE id = id");
            statement.setString(1, name);
            statement.setString(2, player.toString());
            statement.setBytes(3, new ItemStack(Material.IRON_SWORD).serializeAsBytes());
            statement.setBytes(4, ItemStack.serializeItemsAsBytes(new ItemStack[0]));

            statement.executeUpdate();
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
        try (Connection connection = source.getConnection()) {
            connection.setAutoCommit(false);
            PreparedStatement update = connection.prepareStatement("UPDATE kits SET name = ? WHERE id = ?");
            update.setString(1, name);
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
    public void deleteKit(int id) {
        try (Connection connection = source.getConnection()) {
            connection.setAutoCommit(false);
            PreparedStatement statement = connection.prepareStatement("INSERT INTO deleted_kits (SELECT * FROM kits WHERE id = ?)");
            statement.setInt(1, id);
            statement.executeUpdate();

            PreparedStatement statement2 = connection.prepareStatement("DELETE FROM kits WHERE id = ?");
            statement2.setInt(1, id);
            statement2.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
