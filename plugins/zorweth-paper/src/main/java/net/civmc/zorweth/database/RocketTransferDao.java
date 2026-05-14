package net.civmc.zorweth.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import net.civmc.zorweth.transfer.DestinationRocketTransfer;
import net.civmc.zorweth.transfer.RocketBlockPosition;
import net.civmc.zorweth.transfer.RocketChestTransfer;
import net.civmc.zorweth.transfer.RocketEntityPosition;
import net.civmc.zorweth.transfer.RocketManifest;
import net.civmc.zorweth.transfer.RocketPassengerTransfer;
import net.civmc.zorweth.transfer.RocketTransferCargoState;
import net.civmc.zorweth.transfer.RocketTransferPlayerState;
import net.civmc.zorweth.transfer.RocketTransferState;
import org.bukkit.GameMode;

public final class RocketTransferDao {

    private final DataSource dataSource;

    public RocketTransferDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insertPreparedTransfer(final RocketManifest manifest,
                                       final Iterable<RocketPassengerTransfer> passengers,
                                       final Iterable<RocketChestTransfer> chests) throws SQLException {
        try (Connection connection = this.dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                insertTransfer(connection, manifest);
                insertPassengers(connection, passengers);
                insertChests(connection, chests);
                connection.commit();
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public DestinationRocketTransfer getPendingDestinationTransfer(final UUID playerUuid, final String destinationServer)
        throws SQLException {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                 SELECT rt.transfer_id, rt.state, rt.destination_world, rt.fuel_kg,
                     rt.destination_origin_x, rt.destination_origin_y, rt.destination_origin_z,
                     rt.destination_requested_x, rt.destination_requested_z
                 FROM rocket_transfer_players rtp
                 JOIN rocket_transfers rt ON rt.transfer_id = rtp.transfer_id
                 WHERE rtp.player_uuid = ?
                     AND rt.destination_server = ?
                     AND rt.state IN ('SOURCE_CLEARED', 'CLAIMED')
                     AND rtp.state IN ('PENDING', 'CLAIMED')
                 ORDER BY rt.created_at DESC
                 LIMIT 1
                 """)) {
            statement.setString(1, playerUuid.toString());
            statement.setString(2, destinationServer);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                boolean destinationNull = false;
                int destinationOriginX = resultSet.getInt("destination_origin_x");
                destinationNull |= resultSet.wasNull();
                int destinationOriginY = resultSet.getInt("destination_origin_y");
                destinationNull |= resultSet.wasNull();
                int destinationOriginZ = resultSet.getInt("destination_origin_z");
                destinationNull |= resultSet.wasNull();

                return new DestinationRocketTransfer(
                    UUID.fromString(resultSet.getString("transfer_id")),
                    RocketTransferState.valueOf(resultSet.getString("state")),
                    resultSet.getString("destination_world"),
                    destinationNull ? null : new RocketBlockPosition(
                        destinationOriginX,
                        destinationOriginY,
                        destinationOriginZ
                    ),
                    resultSet.getInt("destination_requested_x"),
                    resultSet.getInt("destination_requested_z"),
                    resultSet.getDouble("fuel_kg")
                );
            }
        }
    }

    public boolean setConfirmedDestinationOrigin(final UUID transferId, final RocketBlockPosition origin)
        throws SQLException {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                 UPDATE rocket_transfers
                 SET destination_origin_x = ?, destination_origin_y = ?, destination_origin_z = ?
                 WHERE transfer_id = ?
                 """)) {
            statement.setInt(1, origin.x());
            statement.setInt(2, origin.y());
            statement.setInt(3, origin.z());
            statement.setString(4, transferId.toString());
            return statement.executeUpdate() > 0;
        }
    }

    public RocketPassengerTransfer getPlayer(final UUID transferId, final UUID playerUuid) throws SQLException {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                 SELECT transfer_id, player_uuid, relative_x, relative_y, relative_z, yaw, pitch, inventory,
                     health, xp_level, xp_progress, food_level, saturation, exhaustion, held_slot, game_mode, state
                 FROM rocket_transfer_players
                 WHERE transfer_id = ? AND player_uuid = ?
                 """)) {
            statement.setString(1, transferId.toString());
            statement.setString(2, playerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return readPassenger(resultSet);
            }
        }
    }

    public List<RocketChestTransfer> getChests(final UUID transferId) throws SQLException {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                 SELECT transfer_id, relative_x, relative_y, relative_z, inventory, state
                 FROM rocket_transfer_chests
                 WHERE transfer_id = ?
                 """)) {
            statement.setString(1, transferId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                final List<RocketChestTransfer> chests = new ArrayList<>();
                while (resultSet.next()) {
                    chests.add(new RocketChestTransfer(
                        UUID.fromString(resultSet.getString("transfer_id")),
                        new RocketBlockPosition(
                            resultSet.getInt("relative_x"),
                            resultSet.getInt("relative_y"),
                            resultSet.getInt("relative_z")
                        ),
                        resultSet.getBytes("inventory"),
                        RocketTransferCargoState.valueOf(resultSet.getString("state"))
                    ));
                }
                return chests;
            }
        }
    }

    private RocketPassengerTransfer readPassenger(final ResultSet resultSet) throws SQLException {
        return new RocketPassengerTransfer(
            UUID.fromString(resultSet.getString("transfer_id")),
            UUID.fromString(resultSet.getString("player_uuid")),
            new RocketEntityPosition(
                resultSet.getDouble("relative_x"),
                resultSet.getDouble("relative_y"),
                resultSet.getDouble("relative_z"),
                resultSet.getFloat("yaw"),
                resultSet.getFloat("pitch")
            ),
            resultSet.getBytes("inventory"),
            resultSet.getDouble("health"),
            resultSet.getInt("xp_level"),
            resultSet.getFloat("xp_progress"),
            resultSet.getInt("food_level"),
            resultSet.getFloat("saturation"),
            resultSet.getFloat("exhaustion"),
            resultSet.getInt("held_slot"),
            GameMode.valueOf(resultSet.getString("game_mode")),
            RocketTransferPlayerState.valueOf(resultSet.getString("state"))
        );
    }

    private void insertTransfer(final Connection connection, final RocketManifest manifest) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO rocket_transfers (
                transfer_id, state, source_server, destination_server, destination_world,
                destination_requested_x, destination_requested_z, fuel_kg
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """)) {
            statement.setString(1, manifest.transferId().toString());
            statement.setString(2, RocketTransferState.SOURCE_CLEARED.name());
            statement.setString(3, manifest.sourceServer());
            statement.setString(4, manifest.destinationServer());
            statement.setString(5, manifest.destinationWorld());
            statement.setInt(6, manifest.destinationRequestedX());
            statement.setInt(7, manifest.destinationRequestedZ());
            statement.setDouble(8, manifest.fuelKg());
            statement.executeUpdate();
        }
    }

    private RocketTransferState getTransferStateForUpdate(final Connection connection, final UUID transferId)
        throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT state
            FROM rocket_transfers
            WHERE transfer_id = ?
            FOR UPDATE
            """)) {
            statement.setString(1, transferId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return RocketTransferState.valueOf(resultSet.getString("state"));
            }
        }
    }

    private void updateTransferState(final Connection connection, final UUID transferId, final RocketTransferState state)
        throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            UPDATE rocket_transfers
            SET state = ?
            WHERE transfer_id = ?
            """)) {
            statement.setString(1, state.name());
            statement.setString(2, transferId.toString());
            statement.executeUpdate();
        }
    }

    private boolean claimPassenger(final Connection connection, final UUID transferId, final UUID playerUuid)
        throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            UPDATE rocket_transfer_players
            SET state = 'CLAIMED'
            WHERE transfer_id = ? AND player_uuid = ? AND state IN ('PENDING', 'CLAIMED')
            """)) {
            statement.setString(1, transferId.toString());
            statement.setString(2, playerUuid.toString());
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT 1
            FROM rocket_transfer_players
            WHERE transfer_id = ? AND player_uuid = ? AND state = 'CLAIMED'
            """)) {
            statement.setString(1, transferId.toString());
            statement.setString(2, playerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void insertPassengers(final Connection connection, final Iterable<RocketPassengerTransfer> passengers)
        throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO rocket_transfer_players (
                transfer_id, player_uuid, relative_x, relative_y, relative_z, yaw, pitch, inventory,
                health, xp_level, xp_progress, food_level, saturation, exhaustion, held_slot, game_mode, state
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """)) {
            for (final RocketPassengerTransfer passenger : passengers) {
                statement.setString(1, passenger.transferId().toString());
                statement.setString(2, passenger.playerUuid().toString());
                statement.setDouble(3, passenger.relativePosition().x());
                statement.setDouble(4, passenger.relativePosition().y());
                statement.setDouble(5, passenger.relativePosition().z());
                statement.setFloat(6, passenger.relativePosition().yaw());
                statement.setFloat(7, passenger.relativePosition().pitch());
                statement.setBytes(8, passenger.serializedInventory());
                statement.setDouble(9, passenger.health());
                statement.setInt(10, passenger.xpLevel());
                statement.setFloat(11, passenger.xpProgress());
                statement.setInt(12, passenger.foodLevel());
                statement.setFloat(13, passenger.saturation());
                statement.setFloat(14, passenger.exhaustion());
                statement.setInt(15, passenger.heldSlot());
                statement.setString(16, passenger.gameMode().name());
                statement.setString(17, RocketTransferPlayerState.PENDING.name());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertChests(final Connection connection, final Iterable<RocketChestTransfer> chests)
        throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO rocket_transfer_chests (
                transfer_id, relative_x, relative_y, relative_z, inventory, state
            ) VALUES (?, ?, ?, ?, ?, ?)
            """)) {
            for (final RocketChestTransfer chest : chests) {
                statement.setString(1, chest.transferId().toString());
                statement.setInt(2, chest.relativePosition().x());
                statement.setInt(3, chest.relativePosition().y());
                statement.setInt(4, chest.relativePosition().z());
                statement.setBytes(5, chest.serializedInventory());
                statement.setString(6, RocketTransferCargoState.PENDING.name());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
