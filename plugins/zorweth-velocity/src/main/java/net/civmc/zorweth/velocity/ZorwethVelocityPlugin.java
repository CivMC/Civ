package net.civmc.zorweth.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

@Plugin(id = "zorweth", name = "Zorweth", version = "1.0.0", authors = {"Okx"})
public final class ZorwethVelocityPlugin {

    private static final String TRANSFER_SOURCE_CLEARED = "SOURCE_CLEARED";
    private static final String TRANSFER_CLAIMED = "CLAIMED";
    private static final String TRANSFER_PREPARED = "PREPARED";

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private CommentedConfigurationNode config;
    private HikariDataSource dataSource;
    private RocketTransferRouter router;

    @Inject
    public ZorwethVelocityPlugin(final ProxyServer server, final Logger logger,
                                 @DataDirectory final Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(final ProxyInitializeEvent event) {
        loadConfig();
        this.dataSource = createDataSource();
        this.router = new RocketTransferRouter(this.dataSource);
    }

    @Subscribe
    public void onProxyShutdown(final ProxyShutdownEvent event) {
        if (this.dataSource != null) {
            this.dataSource.close();
            this.dataSource = null;
        }
    }

    @Subscribe(priority = -1)
    public void onServerPreConnect(final ServerPreConnectEvent event) {
        final PendingTransfer transfer;
        try {
            transfer = this.router.getPendingTransfer(event.getPlayer().getUniqueId());
        } catch (final SQLException exception) {
            this.logger.error("Failed to look up pending rocket transfer", exception);
            routeToHoldingOrDisconnect(event);
            return;
        }

        if (transfer == null || TRANSFER_PREPARED.equals(transfer.state())) {
            return;
        }
        if (!TRANSFER_SOURCE_CLEARED.equals(transfer.state()) && !TRANSFER_CLAIMED.equals(transfer.state())) {
            return;
        }

        final Optional<RegisteredServer> destination = this.server.getServer(transfer.destinationServer());
        if (destination.isEmpty()) {
            routeToHoldingOrDisconnect(event);
            return;
        }
        event.setResult(ServerPreConnectEvent.ServerResult.allowed(destination.get()));
    }

    @Subscribe
    public void onServerPostConnect(final ServerPostConnectEvent event) {
        final Optional<ServerConnection> currentServer = event.getPlayer().getCurrentServer();
        if (currentServer.isEmpty()) {
            return;
        }

        try {
            this.router.updateLastServer(
                event.getPlayer().getUniqueId(),
                currentServer.get().getServerInfo().getName()
            );
        } catch (final SQLException exception) {
            this.logger.error("Failed to update player last server", exception);
        }
    }

    private void routeToHoldingOrDisconnect(final ServerPreConnectEvent event) {
        final String holdingServer = this.config.node("holding-server").getString("").trim();
        if (!holdingServer.isEmpty()) {
            final Optional<RegisteredServer> server = this.server.getServer(holdingServer);
            if (server.isPresent()) {
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(server.get()));
                return;
            }
        }
        event.getPlayer().disconnect(Component.text(
            this.config.node("failure-message").getString("Unable to verify your rocket transfer. Please reconnect and try again."),
            NamedTextColor.RED
        ));
    }

    private void loadConfig() {
        try {
            if (!Files.exists(this.dataDirectory)) {
                Files.createDirectories(this.dataDirectory);
            }
            final Path configFile = this.dataDirectory.resolve("config.yml");
            if (!Files.exists(configFile)) {
                try (InputStream input = getClass().getResourceAsStream("/config.yml")) {
                    if (input == null) {
                        throw new IllegalStateException("Default config.yml is missing");
                    }
                    Files.copy(input, configFile);
                }
            }
            this.config = YamlConfigurationLoader.builder().path(configFile).build().load();
        } catch (final IOException exception) {
            throw new RuntimeException("Could not load Zorweth Velocity config", exception);
        }
    }

    private HikariDataSource createDataSource() {
        final CommentedConfigurationNode database = this.config.node("database");
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:" + database.node("driver").getString("mariadb") + "://"
            + database.node("host").getString("localhost") + ":"
            + database.node("port").getInt(3306) + "/"
            + database.node("database").getString("database"));
        hikariConfig.setConnectionTimeout(database.node("connection_timeout").getLong(10_000L));
        hikariConfig.setIdleTimeout(database.node("idle_timeout").getLong(600_000L));
        hikariConfig.setMaxLifetime(database.node("max_lifetime").getLong(7_200_000L));
        hikariConfig.setMaximumPoolSize(database.node("poolsize").getInt(5));
        hikariConfig.setUsername(database.node("user").getString("root"));
        final String password = database.node("password").getString("");
        if (!password.isBlank()) {
            hikariConfig.setPassword(password);
        }
        return new HikariDataSource(hikariConfig);
    }

    private record PendingTransfer(String state, String destinationServer) {
    }

    private static final class RocketTransferRouter {

        private final HikariDataSource dataSource;

        private RocketTransferRouter(final HikariDataSource dataSource) {
            this.dataSource = dataSource;
        }

        private PendingTransfer getPendingTransfer(final UUID playerUuid) throws SQLException {
            try (Connection connection = this.dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("""
                     SELECT rt.state, rt.destination_server
                     FROM rocket_transfer_players rtp
                     JOIN rocket_transfers rt ON rt.transfer_id = rtp.transfer_id
                     WHERE rtp.player_uuid = ?
                         AND rt.state IN ('PREPARED', 'SOURCE_CLEARED', 'CLAIMED')
                         AND rtp.state IN ('PENDING', 'CLAIMED')
                     ORDER BY rt.created_at DESC
                     LIMIT 1
                     """)) {
                statement.setString(1, playerUuid.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }
                    return new PendingTransfer(
                        resultSet.getString("state"),
                        resultSet.getString("destination_server")
                    );
                }
            }
        }

        private void updateLastServer(final UUID playerUuid, final String serverName) throws SQLException {
            try (Connection connection = this.dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO player_server_state (player_uuid, last_server)
                     VALUES (?, ?)
                     ON DUPLICATE KEY UPDATE last_server = VALUES(last_server)
                     """)) {
                statement.setString(1, playerUuid.toString());
                statement.setString(2, serverName);
                statement.executeUpdate();
            }
        }
    }
}
