package net.civmc.zorweth.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import net.civmc.zorweth.transfer.DestinationRocketTransfer;
import net.civmc.zorweth.transfer.RocketBlockPosition;
import net.civmc.zorweth.transfer.RocketChestTransfer;
import net.civmc.zorweth.transfer.RocketManifest;
import net.civmc.zorweth.transfer.RocketPassengerTransfer;
import net.civmc.zorweth.transfer.RocketTransferCargoState;
import net.civmc.zorweth.transfer.RocketTransferPlayerState;
import net.civmc.zorweth.transfer.RocketTransferState;

public final class RocketTransferDao {

    private final DataSource dataSource;

    public RocketTransferDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insertPreparedTransfer(final RocketManifest manifest, final RocketBlockPosition destinationOrigin,
                                       final Iterable<RocketPassengerTransfer> passengers,
                                       final Iterable<RocketChestTransfer> chests) throws SQLException {
        try (Connection connection = this.dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                insertTransfer(connection, manifest, destinationOrigin);
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

    public boolean transitionTransferState(final RocketManifest manifest, final RocketTransferState from,
                                           final RocketTransferState to) throws SQLException {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                 UPDATE rocket_transfers
                 SET state = ?
                 WHERE transfer_id = ? AND state = ?
                 """)) {
            statement.setString(1, to.name());
            statement.setString(2, manifest.transferId().toString());
            statement.setString(3, from.name());
            return statement.executeUpdate() == 1;
        }
    }

    public DestinationRocketTransfer getPendingDestinationTransfer(final UUID playerUuid, final String destinationServer)
        throws SQLException {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                 SELECT rt.transfer_id, rt.state, rt.destination_world,
                     rt.destination_origin_x, rt.destination_origin_y, rt.destination_origin_z
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
                return new DestinationRocketTransfer(
                    UUID.fromString(resultSet.getString("transfer_id")),
                    RocketTransferState.valueOf(resultSet.getString("state")),
                    resultSet.getString("destination_world"),
                    new RocketBlockPosition(
                        resultSet.getInt("destination_origin_x"),
                        resultSet.getInt("destination_origin_y"),
                        resultSet.getInt("destination_origin_z")
                    )
                );
            }
        }
    }

    public boolean claimDestinationTransfer(final UUID transferId, final UUID playerUuid) throws SQLException {
        try (Connection connection = this.dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                final RocketTransferState transferState = getTransferStateForUpdate(connection, transferId);
                if (transferState == null || (transferState != RocketTransferState.SOURCE_CLEARED
                    && transferState != RocketTransferState.CLAIMED)) {
                    connection.rollback();
                    return false;
                }
                if (transferState == RocketTransferState.SOURCE_CLEARED) {
                    updateTransferState(connection, transferId, RocketTransferState.CLAIMED);
                }
                if (!claimPassenger(connection, transferId, playerUuid)) {
                    connection.rollback();
                    return false;
                }
                connection.commit();
                return true;
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void insertTransfer(final Connection connection, final RocketManifest manifest,
                                final RocketBlockPosition destinationOrigin) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO rocket_transfers (
                transfer_id, state, source_server, destination_server, source_world, destination_world,
                source_origin_x, source_origin_y, source_origin_z,
                destination_origin_x, destination_origin_y, destination_origin_z,
                destination_requested_x, destination_requested_z
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """)) {
            statement.setString(1, manifest.transferId().toString());
            statement.setString(2, RocketTransferState.PREPARED.name());
            statement.setString(3, manifest.sourceServer());
            statement.setString(4, manifest.destinationServer());
            statement.setString(5, manifest.sourceWorld());
            statement.setString(6, manifest.destinationWorld());
            statement.setInt(7, manifest.sourceOrigin().x());
            statement.setInt(8, manifest.sourceOrigin().y());
            statement.setInt(9, manifest.sourceOrigin().z());
            statement.setInt(10, destinationOrigin.x());
            statement.setInt(11, destinationOrigin.y());
            statement.setInt(12, destinationOrigin.z());
            statement.setInt(13, manifest.destinationRequestedX());
            statement.setInt(14, manifest.destinationRequestedZ());
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
