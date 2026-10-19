package net.civmc.zorweth.database;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import net.civmc.nameapi.Migrator;

public final class ZorwethDatabase {

    private ZorwethDatabase() {
    }

    public static void migrate(final DataSource dataSource) throws SQLException {
        final Migrator migrator = new Migrator();
        migrator.registerMigration("zorweth", 0,
            """
                CREATE TABLE IF NOT EXISTS rocket_transfers (
                    transfer_id VARCHAR(36) NOT NULL,
                    source_server VARCHAR(64) NOT NULL,
                    destination_server VARCHAR(64) NOT NULL,
                    destination_world VARCHAR(64) NOT NULL,
                    destination_origin_x INT,
                    destination_origin_y INT,
                    destination_origin_z INT,
                    destination_requested_x INT NOT NULL,
                    destination_requested_z INT NOT NULL,
                    pilot_uuid VARCHAR(36) NOT NULL,
                    flight_computer_group_id INT,
                    fuel_kg DOUBLE NOT NULL,
                    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                    PRIMARY KEY (transfer_id),
                    INDEX idx_rocket_transfers_destination (destination_server)
                )
                """,
            """
                CREATE TABLE IF NOT EXISTS rocket_transfer_players (
                    transfer_id VARCHAR(36) NOT NULL,
                    player_uuid VARCHAR(36) NOT NULL,
                    relative_x DOUBLE NOT NULL,
                    relative_y DOUBLE NOT NULL,
                    relative_z DOUBLE NOT NULL,
                    yaw FLOAT NOT NULL,
                    pitch FLOAT NOT NULL,
                    inventory LONGBLOB NOT NULL,
                    health DOUBLE NOT NULL,
                    xp_level INT NOT NULL,
                    xp_progress FLOAT NOT NULL,
                    food_level INT NOT NULL,
                    saturation FLOAT NOT NULL,
                    exhaustion FLOAT NOT NULL,
                    held_slot INT NOT NULL,
                    game_mode VARCHAR(32) NOT NULL,
                    state VARCHAR(32) NOT NULL DEFAULT 'PENDING',
                    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                    PRIMARY KEY (transfer_id, player_uuid),
                    INDEX idx_rocket_transfer_players_player (player_uuid, state),
                    CONSTRAINT fk_rocket_transfer_players_transfer FOREIGN KEY (transfer_id)
                        REFERENCES rocket_transfers (transfer_id) ON DELETE CASCADE
                )
                """,
            """
                CREATE TABLE IF NOT EXISTS rocket_transfer_chests (
                    transfer_id VARCHAR(36) NOT NULL,
                    relative_x INT NOT NULL,
                    relative_y INT NOT NULL,
                    relative_z INT NOT NULL,
                    inventory LONGBLOB NOT NULL,
                    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                    PRIMARY KEY (transfer_id, relative_x, relative_y, relative_z),
                    CONSTRAINT fk_rocket_transfer_chests_transfer FOREIGN KEY (transfer_id)
                        REFERENCES rocket_transfers (transfer_id) ON DELETE CASCADE
                )
                """,
            """
                CREATE TABLE IF NOT EXISTS rocket_player_routes (
                    player_uuid VARCHAR(36) NOT NULL,
                    expected_server VARCHAR(64) NOT NULL,
                    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                    PRIMARY KEY (player_uuid)
                )
                """);
        migrator.registerMigration("zorweth", 1,
            """
                CREATE TABLE IF NOT EXISTS cross_server_ott_arrivals (
                    requester_uuid VARCHAR(36) NOT NULL,
                    target_uuid VARCHAR(36) NOT NULL,
                    target_server VARCHAR(64) NOT NULL,
                    target_world VARCHAR(64) NOT NULL,
                    target_x DOUBLE NOT NULL,
                    target_y DOUBLE NOT NULL,
                    target_z DOUBLE NOT NULL,
                    target_yaw FLOAT NOT NULL,
                    target_pitch FLOAT NOT NULL,
                    expires_at DATETIME(3) NOT NULL,
                    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                    PRIMARY KEY (requester_uuid),
                    INDEX idx_cross_server_ott_arrivals_target (target_uuid, target_server),
                    INDEX idx_cross_server_ott_arrivals_expiry (expires_at)
                )
                """);
        migrator.registerMigration("zorweth", 2,
            """
                ALTER TABLE rocket_transfers
                ADD COLUMN uses_remaining INT NOT NULL DEFAULT 6
                """);

        try (Connection connection = dataSource.getConnection()) {
            migrator.migrate(connection);
        }
    }
}
