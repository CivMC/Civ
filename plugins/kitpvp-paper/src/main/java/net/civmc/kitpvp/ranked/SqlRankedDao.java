package net.civmc.kitpvp.ranked;

import net.civmc.kitpvp.KitPvpPlugin;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class SqlRankedDao implements RankedDao {

    private final ManagedDatasource source;

    public SqlRankedDao(ManagedDatasource source) {
        this.source = source;
        source.registerMigration(7, false, """
               CREATE TABLE IF NOT EXISTS ranked_kits (
                    player VARCHAR(36),
                    kit INT,
                    PRIMARY KEY (player)
                )
            """, """
               CREATE TABLE IF NOT EXISTS ranked_elo (
                    player VARCHAR(36),
                    elo DOUBLE NOT NULL,
                    INDEX (elo),
                    PRIMARY KEY (player)
                )
            """, """
                CREATE TABLE IF NOT EXISTS ranked_matches (
                    player VARCHAR(36) NOT NULL,
                    opponent VARCHAR(36) NOT NULL,
                    player_elo DOUBLE NOT NULL,
                    opponent_elo DOUBLE NOT NULL,
                    winner VARCHAR(36) NOT NULL,
                    timestamp TIMESTAMP NOT NULL,
                    INDEX (timestamp)
                )
            """);
    }

    @Override
    public int getKit(UUID player) {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT kit FROM ranked_kits WHERE player = ?");
            statement.setString(1, player.toString());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("kit");
            } else {
                return -1;
            }
        } catch (SQLException ex) {
            JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Error getting kit", ex);
            return -1;
        }
    }

    @Override
    public void setKit(UUID player, int kit) {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("REPLACE INTO ranked_kits (player, kit) VALUES (?, ?)");
            statement.setString(1, player.toString());
            statement.setInt(2, kit);

            statement.executeUpdate();
        } catch (SQLException ex) {
            JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Error setting kit", ex);
        }
    }

    @Override
    public double getElo(UUID player) {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT elo FROM ranked_elo WHERE player = ?");
            statement.setString(1, player.toString());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("elo");
            } else {
                return 1000;
            }
        } catch (SQLException ex) {
            JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Error getting elo", ex);
            return -1;
        }
    }

    @Override
    public void updateElo(UUID player, UUID opponent, UUID winner) {
        try (Connection connection = source.getConnection()) {
            connection.setAutoCommit(false);
            PreparedStatement statement = connection.prepareStatement("SELECT player, elo FROM ranked_elo WHERE player IN (?, ?) FOR UPDATE");
            statement.setString(1, player.toString());
            statement.setString(2, opponent.toString());

            Map<UUID, Double> elos = new HashMap<>();
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                elos.put(UUID.fromString(resultSet.getString("player")), resultSet.getDouble("elo"));
            }

            double playerElo = elos.getOrDefault(player, 1000D);
            double opponentElo = elos.getOrDefault(opponent, 1000D);
            if (winner == null) {
                playerElo += Elo.getChange(playerElo, opponentElo).draw();
                opponentElo += Elo.getChange(opponentElo, playerElo).draw();
            } else if (winner.equals(player)) {
                playerElo += Elo.getChange(playerElo, opponentElo).win();
                opponentElo += Elo.getChange(opponentElo, playerElo).loss();
            } else {
                playerElo += Elo.getChange(playerElo, opponentElo).loss();
                opponentElo += Elo.getChange(opponentElo, playerElo).win();
            }
            playerElo = Math.max(playerElo, 200);
            opponentElo = Math.max(opponentElo, 200);

            PreparedStatement update = connection.prepareStatement("REPLACE INTO ranked_elo (player, elo) VALUES (?, ?)");
            update.setString(1, player.toString());
            update.setDouble(2, playerElo);
            update.addBatch();
            update.setString(1, opponent.toString());
            update.setDouble(2, opponentElo);
            update.addBatch();
            update.executeBatch();

            PreparedStatement match = connection.prepareStatement("INSERT INTO ranked_matches (player, opponent, player_elo, opponent_elo, winner) VALUES (?, ?, ?, ?, ?)");
            match.setString(1, player.toString());
            match.setString(2, opponent.toString());
            match.setDouble(3, elos.getOrDefault(player, 1000D));
            match.setDouble(4, elos.getOrDefault(opponent, 1000D));
            match.setString(5, winner == null ? null : winner.toString());
            match.executeUpdate();

            connection.commit();
        } catch (SQLException ex) {
            JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Error updating elo", ex);
        }
    }

    @Override
    public List<Rank> getTop(int n) {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT player, elo FROM ranked_elo ORDER BY elo DESC LIMIT ?");
            statement.setInt(1, n);

            ResultSet resultSet = statement.executeQuery();
            List<Rank> players = new ArrayList<>();
            while (resultSet.next()) {
                players.add(new Rank(UUID.fromString(resultSet.getString("player")), resultSet.getDouble("elo")));
            }
            return players;
        } catch (SQLException ex) {
            JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Error getting top players", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<UUID, Double> getAll() {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT player, elo FROM ranked_elo");

            ResultSet resultSet = statement.executeQuery();
            Map<UUID, Double> players = new HashMap<>();
            while (resultSet.next()) {
                players.put(UUID.fromString(resultSet.getString("player")), resultSet.getDouble("elo"));
            }
            return players;
        } catch (SQLException ex) {
            JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Error getting top players", ex);
            return Collections.emptyMap();
        }
    }
}
