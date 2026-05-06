package net.civmc.namelayer.velocity.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public record NameLayerVelocityConfig(DatabaseConfig database, RabbitMqConfig rabbitMq, Map<String, String> serverDatabases) {

    public static @Nullable NameLayerVelocityConfig load(
        final Path dataDirectory,
        final Logger logger,
        final Class<?> pluginClass
    ) {
        final CommentedConfigurationNode root = loadRootNode(dataDirectory, logger, pluginClass);
        if (root == null) {
            return null;
        }
        final CommentedConfigurationNode database = root.node("database");
        final CommentedConfigurationNode rabbitMq = root.node("rabbitmq");
        return new NameLayerVelocityConfig(
            new DatabaseConfig(
                database.node("driver").getString("mariadb"),
                database.node("host").getString("localhost"),
                database.node("port").getInt(3306),
                database.node("user").getString("root"),
                database.node("password").getString(""),
                database.node("poolsize").getInt(10),
                database.node("connection_timeout").getLong(10_000L),
                database.node("idle_timeout").getLong(600_000L),
                database.node("max_lifetime").getLong(7_200_000L)
            ),
            new RabbitMqConfig(
                rabbitMq.node("user").getString("guest"),
                rabbitMq.node("password").getString("guest"),
                rabbitMq.node("host").getString("localhost"),
                rabbitMq.node("port").getInt(5672)
            ),
            root.node("server_databases").childrenMap()
                .entrySet().stream()
                .map(v -> new AbstractMap.SimpleEntry<>(v.getKey(), v.getValue().getString()))
                .collect(Collectors.toMap(v -> v.getKey().toString(), AbstractMap.SimpleEntry::getValue))
        );
    }

    private static @Nullable CommentedConfigurationNode loadRootNode(
        final Path dataDirectory,
        final Logger logger,
        final Class<?> pluginClass
    ) {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
        } catch (final IOException exception) {
            logger.error("Could not create data directory: {}", dataDirectory, exception);
            return null;
        }

        final Path configFile = dataDirectory.resolve("config.yml");
        if (!Files.exists(configFile)) {
            try (InputStream input = pluginClass.getResourceAsStream("/config.yml")) {
                if (input == null) {
                    logger.error("Default NameLayer Velocity configuration is missing");
                    return null;
                }
                Files.copy(input, configFile);
            } catch (final IOException exception) {
                logger.error("Could not create default configuration file: {}", configFile, exception);
                return null;
            }
        }

        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(configFile).build();
        try {
            return loader.load();
        } catch (final IOException exception) {
            logger.error("Could not load configuration file: {}", configFile, exception);
            return null;
        }
    }
}
