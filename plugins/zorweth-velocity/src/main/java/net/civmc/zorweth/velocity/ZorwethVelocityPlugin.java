package net.civmc.zorweth.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
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
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

@Plugin(id = "zorweth", name = "Zorweth", version = "1.0.0", authors = {"Okx"})
public final class ZorwethVelocityPlugin {

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
        registerCommands();
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
        if (canBypass(event.getPlayer())) {
            return;
        }

        final RegisteredServer target = event.getResult().getServer().orElse(event.getOriginalServer());
        final String targetName = target.getServerInfo().getName();
        final String originalName = event.getOriginalServer().getServerInfo().getName();
        if (!isProtectedServer(targetName) && !isProtectedServer(originalName)) {
            return;
        }

        final String expectedServer;
        try {
            expectedServer = getExpectedServer(event.getPlayer().getUniqueId());
        } catch (final SQLException exception) {
            this.logger.error("Failed to look up rocket player route", exception);
            routeToHoldingOrDisconnect(event);
            return;
        }

        if (targetName.equals(expectedServer)) {
            return;
        }

        forceExpectedServer(event, expectedServer);
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

    private void forceExpectedServer(final ServerPreConnectEvent event, final String expectedServer) {
        final Optional<RegisteredServer> expected = this.server.getServer(expectedServer);
        if (expected.isEmpty()) {
            this.logger.error("Configured rocket route server {} does not exist", expectedServer);
            routeToHoldingOrDisconnect(event);
            return;
        }
        event.setResult(ServerPreConnectEvent.ServerResult.allowed(expected.get()));
    }

    public boolean isProtectedServer(final String serverName) {
        return serverName.equals(getMainServer()) || serverName.equals(getZorwethServer());
    }

    public boolean canBypass(final Player player) {
        final String permission = this.config.node("admin-bypass-permission").getString("zorweth.admin");
        return !permission.isBlank() && player.hasPermission(permission);
    }

    public String getExpectedServer(final UUID playerId) throws SQLException {
        final PlayerRoute route = this.router.getPlayerRoute(playerId);
        return route == null ? getDefaultServer() : route.expectedServer();
    }

    public void setExpectedServer(final UUID playerId, final String expectedServer) throws SQLException {
        if (!isProtectedServer(expectedServer)) {
            throw new IllegalArgumentException("Expected server must be " + getMainServer() + " or " + getZorwethServer());
        }
        this.router.setPlayerRoute(playerId, expectedServer);
    }

    public String getDefaultServer() {
        return this.config.node("default-server").getString(getMainServer());
    }

    public String getMainServer() {
        return this.config.node("main-server").getString("main");
    }

    public String getZorwethServer() {
        return this.config.node("zorweth-server").getString("zorweth");
    }

    private void registerCommands() {
        final CommandManager commandManager = this.server.getCommandManager();
        final CommandMeta routeMeta = commandManager.metaBuilder("zorwethroute")
            .plugin(this)
            .build();
        commandManager.register(routeMeta, new RouteCommand());
    }

    private UUID parsePlayerId(final String input) throws SQLException {
        try {
            return UUID.fromString(input);
        } catch (final IllegalArgumentException ignored) {
            final Optional<Player> onlinePlayer = this.server.getPlayer(input);
            if (onlinePlayer.isPresent()) {
                return onlinePlayer.get().getUniqueId();
            }
            return this.router.getPlayerId(input);
        }
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
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

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

    private record PlayerRoute(String expectedServer) {
    }

    private final class RouteCommand implements SimpleCommand {

        @Override
        public void execute(final Invocation invocation) {
            final CommandSource source = invocation.source();
            if (!source.hasPermission("zorweth.admin")) {
                source.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
                return;
            }
            final String[] args = invocation.arguments();
            if (args.length != 2) {
                source.sendMessage(Component.text("Usage: /zorwethroute <player|uuid> <main|zorweth>", NamedTextColor.RED));
                return;
            }

            final String expectedServer = args[1];
            if (!isProtectedServer(expectedServer)) {
                source.sendMessage(Component.text("Server must be " + getMainServer() + " or " + getZorwethServer(), NamedTextColor.RED));
                return;
            }

            CompletableFuture.runAsync(() -> {
                final UUID playerId;
                try {
                    playerId = parsePlayerId(args[0]);
                } catch (final SQLException exception) {
                    source.sendMessage(Component.text("Failed to look up player.", NamedTextColor.RED));
                    logger.error("Failed to look up player for Zorweth route override", exception);
                    return;
                }

                if (playerId == null) {
                    source.sendMessage(Component.text("Unknown player. Use a UUID for offline players not in NameAPI.", NamedTextColor.RED));
                    return;
                }

                try {
                    setExpectedServer(playerId, expectedServer);
                } catch (final SQLException exception) {
                    source.sendMessage(Component.text("Failed to update route.", NamedTextColor.RED));
                    logger.error("Failed to override Zorweth route", exception);
                    return;
                }
                source.sendMessage(Component.text("Set " + playerId + " expected server to " + expectedServer + ".", NamedTextColor.GREEN));
            });
        }

        @Override
        public boolean hasPermission(final Invocation invocation) {
            return invocation.source().hasPermission("zorweth.admin");
        }
    }

    private static final class RocketTransferRouter {

        private final HikariDataSource dataSource;

        private RocketTransferRouter(final HikariDataSource dataSource) {
            this.dataSource = dataSource;
        }

        private PlayerRoute getPlayerRoute(final UUID playerUuid) throws SQLException {
            try (Connection connection = this.dataSource.getConnection();
                  PreparedStatement statement = connection.prepareStatement("""
                      SELECT expected_server
                      FROM rocket_player_routes
                      WHERE player_uuid = ?
                      """)) {
                statement.setString(1, playerUuid.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }
                    return new PlayerRoute(resultSet.getString("expected_server"));
                }
            }
        }

        private UUID getPlayerId(final String playerName) throws SQLException {
            try (Connection connection = this.dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("""
                     SELECT uuid
                     FROM Name_player
                     WHERE player = ?
                     LIMIT 1
                     """)) {
                statement.setString(1, playerName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }
                    return UUID.fromString(resultSet.getString("uuid"));
                }
            }
        }

        private void setPlayerRoute(final UUID playerUuid, final String expectedServer) throws SQLException {
            try (Connection connection = this.dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO rocket_player_routes (player_uuid, expected_server)
                     VALUES (?, ?)
                     ON DUPLICATE KEY UPDATE
                         expected_server = VALUES(expected_server)
                     """)) {
                statement.setString(1, playerUuid.toString());
                statement.setString(2, expectedServer);
                statement.executeUpdate();
            }
        }
    }
}
