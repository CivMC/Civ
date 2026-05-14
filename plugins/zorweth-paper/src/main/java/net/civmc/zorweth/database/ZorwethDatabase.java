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
                    state VARCHAR(32) NOT NULL,
                    source_server VARCHAR(64) NOT NULL,
                    destination_server VARCHAR(64) NOT NULL,
                    destination_world VARCHAR(64) NOT NULL,
                    destination_origin_x INT,
                    destination_origin_y INT,
                    destination_origin_z INT,
                    destination_requested_x INT NOT NULL,
                    destination_requested_z INT NOT NULL,
                    fuel_kg DOUBLE NOT NULL,
                    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                    PRIMARY KEY (transfer_id),
                    INDEX idx_rocket_transfers_state (state),
                    INDEX idx_rocket_transfers_destination (destination_server, state)
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
                    state VARCHAR(32) NOT NULL DEFAULT 'PENDING',
                    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                    PRIMARY KEY (transfer_id, relative_x, relative_y, relative_z),
                    CONSTRAINT fk_rocket_transfer_chests_transfer FOREIGN KEY (transfer_id)
                        REFERENCES rocket_transfers (transfer_id) ON DELETE CASCADE
                )
                """,
            """
                CREATE TABLE IF NOT EXISTS player_server_state (
                    player_uuid VARCHAR(36) NOT NULL,
                    last_server VARCHAR(64) NOT NULL,
                    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                    PRIMARY KEY (player_uuid),
                    INDEX idx_player_server_state_last_server (last_server)
                )
                """);

        try (Connection connection = dataSource.getConnection()) {
            migrator.migrate(connection);
        }
    }
}
