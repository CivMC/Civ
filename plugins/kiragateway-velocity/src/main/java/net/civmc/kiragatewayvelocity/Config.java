package net.civmc.kiragatewayvelocity;

import com.rabbitmq.client.ConnectionFactory;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    /**
     * Loads the config from disk, and creates it if necessary
     */
    public static @Nullable CommentedConfigurationNode loadConfig() {
        KiraGateway plugin = KiraGateway.getInstance();

        try {
            // ensure data directory exists
            if (!Files.exists(plugin.dataDirectory)) {
                Files.createDirectories(plugin.dataDirectory);
            }
        } catch (IOException e) {
            plugin.logger.error("Could not create data directory at {}", plugin.dataDirectory);
            plugin.logger.error("Failed to create data directory", e);
            return null;
        }

        // create config file if it doesn't exist
        Path configFile = plugin.dataDirectory.resolve("config.yml");
        if (!Files.exists(configFile)) {
            try (InputStream in = plugin.getClass().getResourceAsStream("/config.yml")) {
                if (in != null) {
                    Files.copy(in, configFile);
                    plugin.logger.info("Default configuration file created.");
                } else {
                    plugin.logger.error("Default configuration file is missing in resources!");
                    return null;
                }
            } catch (IOException e) {
                plugin.logger.error("Could not create default configuration file at {}", configFile);
                plugin.logger.error("Failed to create default config", e);
            }
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(configFile).build();
        try {
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not load configuration file: " + configFile, e);
        }
    }

    /**
     * Reads the RabbitMQ configuration from the config file
     * @return
     */
    public static @Nullable ConnectionFactory getRabbitConfig(CommentedConfigurationNode config) {
        if (config == null) {
            return null;
        }

        ConnectionFactory connFac = new ConnectionFactory();
        var user = config.node("rabbitmq", "user");
        if (!user.empty()) {
            connFac.setUsername(user.getString());
        }
        var password = config.node("rabbitmq", "password");
        if (!password.empty()) {
            connFac.setPassword(password.getString());
        }
        var host = config.node("rabbitmq", "host");
        if (!host.empty()) {
            connFac.setHost(host.getString());
        }
        var port = config.node("rabbitmq", "port");
        if (!port.empty() && port.getInt(-1) != -1) {
            connFac.setPort(port.getInt());
        }
        return connFac;
    }
}
