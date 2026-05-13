package net.civmc.zorweth.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
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
