package xyz.huskydog.banstickCore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public class Config {

    private final BanstickPlugin plugin;

    private final @NotNull CommentedConfigurationNode config;

    public Config(@NotNull BanstickPlugin plugin) {
        this.plugin = plugin;
        config = loadConfig();
    }

    /**
     * Loads the config from disk, and creates it if necessary
     */
    private @NotNull CommentedConfigurationNode loadConfig() {
        Path dataDirectory = plugin.getDataDirectory();

        try {
            // ensure data directory exists
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
        } catch (IOException e) {
            // logger.error("Could not create data directory at {}", dataDirectory);
            // logger.error("Failed to create data directory", e);
            throw new RuntimeException("Could not create data directory at " + dataDirectory, e);
        }

        // create config file if it doesn't exist
        Path configFile = dataDirectory.resolve("config.yml");
        if (!Files.exists(configFile)) {
            try (InputStream in = plugin.getClass().getResourceAsStream("/config.yml")) {
                if (in != null) {
                    Files.copy(in, configFile);
                    plugin.getLogger().info("Default configuration file created.");
                } else {
                    // logger.error("Default configuration file is missing in resources!");
                    throw new RuntimeException("Default configuration file is missing in resources!");
                }
            } catch (IOException e) {
                // logger.error("Could not create default configuration file at {}", configFile);
                // logger.error("Failed to create default config", e);
                throw new RuntimeException("Could not create default configuration file at " + configFile, e);
            }
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(configFile).build();
        try {
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not load configuration file: " + configFile, e);
        }
    }

    public @NotNull CommentedConfigurationNode getRawConfig() {
        return config;
    }
}
