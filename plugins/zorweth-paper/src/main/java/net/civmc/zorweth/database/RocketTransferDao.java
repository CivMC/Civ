package net.civmc.zorweth.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
import org.bukkit.GameMode;

public final class RocketTransferDao {

    private static final String PLAYER_STATE_PENDING = "PENDING";

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
                upsertRoutes(connection, manifest, passengers);
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
                 SELECT rt.transfer_id, rt.destination_world, rt.fuel_kg,
                      rt.destination_origin_x, rt.destination_origin_y, rt.destination_origin_z,
                      rt.destination_requested_x, rt.destination_requested_z, rt.pilot_uuid,
                      rt.flight_computer_group_id
                 FROM rocket_transfer_players rtp
                 JOIN rocket_transfers rt ON rt.transfer_id = rtp.transfer_id
                 WHERE rtp.player_uuid = ?
                     AND rt.destination_server = ?
                     AND rtp.state = 'PENDING'
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
                final int flightComputerGroupId = resultSet.getInt("flight_computer_group_id");
                final Integer nullableFlightComputerGroupId = resultSet.wasNull() ? null : flightComputerGroupId;
                final String pilotUuid = resultSet.getString("pilot_uuid");

                return new DestinationRocketTransfer(
                    UUID.fromString(resultSet.getString("transfer_id")),
                    resultSet.getString("destination_world"),
                    destinationNull ? null : new RocketBlockPosition(
                        destinationOriginX,
                        destinationOriginY,
                        destinationOriginZ
                    ),
                    resultSet.getInt("destination_requested_x"),
                    resultSet.getInt("destination_requested_z"),
                    UUID.fromString(pilotUuid == null ? playerUuid.toString() : pilotUuid),
                    nullableFlightComputerGroupId,
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
                     health, xp_level, xp_progress, food_level, saturation, exhaustion, held_slot, game_mode
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

    public boolean markPassengerApplied(final UUID transferId, final UUID playerUuid) throws SQLException {
        try (Connection connection = this.dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement("""
                    UPDATE rocket_transfer_players
                    SET state = 'APPLIED'
                    WHERE transfer_id = ? AND player_uuid = ? AND state = 'PENDING'
                    """)) {
                    statement.setString(1, transferId.toString());
                    statement.setString(2, playerUuid.toString());
                    statement.executeUpdate();
                }

                final boolean applied;
                try (PreparedStatement statement = connection.prepareStatement("""
                    SELECT 1
                    FROM rocket_transfer_players
                    WHERE transfer_id = ? AND player_uuid = ? AND state = 'APPLIED'
                    """)) {
                    statement.setString(1, transferId.toString());
                    statement.setString(2, playerUuid.toString());
                    try (ResultSet resultSet = statement.executeQuery()) {
                        applied = resultSet.next();
                    }
                }
                connection.commit();
                return applied;
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<RocketChestTransfer> getChests(final UUID transferId) throws SQLException {
        try (Connection connection = this.dataSource.getConnection();
              PreparedStatement statement = connection.prepareStatement("""
                 SELECT transfer_id, relative_x, relative_y, relative_z, inventory
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
                        resultSet.getBytes("inventory")
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
            GameMode.valueOf(resultSet.getString("game_mode"))
        );
    }

    private void insertTransfer(final Connection connection, final RocketManifest manifest) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO rocket_transfers (
                transfer_id, source_server, destination_server, destination_world,
                destination_requested_x, destination_requested_z, pilot_uuid, flight_computer_group_id, fuel_kg
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """)) {
            statement.setString(1, manifest.transferId().toString());
            statement.setString(2, manifest.sourceServer());
            statement.setString(3, manifest.destinationServer());
            statement.setString(4, manifest.destinationWorld());
            statement.setInt(5, manifest.destinationRequestedX());
            statement.setInt(6, manifest.destinationRequestedZ());
            statement.setString(7, manifest.pilotUuid().toString());
            if (manifest.flightComputerGroupId() == null) {
                statement.setNull(8, Types.INTEGER);
            } else {
                statement.setInt(8, manifest.flightComputerGroupId());
            }
            statement.setDouble(9, manifest.fuelKg());
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
                statement.setString(17, PLAYER_STATE_PENDING);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void upsertRoutes(final Connection connection, final RocketManifest manifest,
                              final Iterable<RocketPassengerTransfer> passengers) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO rocket_player_routes (player_uuid, expected_server)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE
                expected_server = VALUES(expected_server)
            """)) {
            for (final RocketPassengerTransfer passenger : passengers) {
                statement.setString(1, passenger.playerUuid().toString());
                statement.setString(2, manifest.destinationServer());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertChests(final Connection connection, final Iterable<RocketChestTransfer> chests)
        throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO rocket_transfer_chests (
                transfer_id, relative_x, relative_y, relative_z, inventory
            ) VALUES (?, ?, ?, ?, ?)
            """)) {
            for (final RocketChestTransfer chest : chests) {
                statement.setString(1, chest.transferId().toString());
                statement.setInt(2, chest.relativePosition().x());
                statement.setInt(3, chest.relativePosition().y());
                statement.setInt(4, chest.relativePosition().z());
                statement.setBytes(5, chest.serializedInventory());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
