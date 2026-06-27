package net.civmc.civproxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.zaxxer.hikari.HikariConfig;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import com.zaxxer.hikari.HikariDataSource;
import net.civmc.civproxy.renamer.PlayerRenamer;
import net.civmc.nameapi.NameAPI;
import net.civmc.zorweth.velocity.ZorwethVelocityPlugin;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

@Plugin(id = "civproxy", name = "CivProxy", version = "1.0.0", authors = {"Okx"}, dependencies = {@Dependency(id = "ajqueue"), @Dependency(id = "luckperms"), @Dependency(id = "zorweth")})
public class CivProxyPlugin {

    private final ProxyServer server;
    private final Logger logger;

    private CommentedConfigurationNode config;

    private NameAPI nameAPI;
    @Inject
    public CivProxyPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;

        loadConfig(dataDirectory);
    }

    public Logger getLogger() {
        return logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadNameApiConfig();
        new PlayerCount(this, server).start();
        new PlayerRenamer(this, server, this.nameAPI).start();
        final Optional<ZorwethVelocityPlugin> zorweth = server.getPluginManager().getPlugin("zorweth")
            .flatMap(PluginContainer::getInstance)
            .map(ZorwethVelocityPlugin.class::cast);
        if (zorweth.isPresent()) {
            zorweth.get().setOfflinePlayerResolver(this.nameAPI::getUUID);
            if (server.getPluginManager().isLoaded("ajqueue")) {
                new QueueListener(this, server, zorweth.get()).start();
            }
        } else {
            this.logger.error("Zorweth is required for route management, but its plugin instance was not available");
        }
    }

    private void loadNameApiConfig() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        CommentedConfigurationNode database = config.node("database");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:" + database.node("driver").getString("mariadb") + "://" + database.node("host").getString("localhost") + ":" +
            database.node("port").getInt(3306) + "/" + database.node("database").getString("minecraft"));
        config.setConnectionTimeout(database.node("connection_timeout").getInt(10_000));
        config.setIdleTimeout(database.node("idle_timeout").getInt(600_000));
        config.setMaxLifetime(database.node("max_lifetime").getInt(7_200_000));
        config.setMaximumPoolSize(database.node("poolsize").getInt(10));
        config.setUsername(database.node("user").getString("root"));
        String password = database.node("password").getString();
        if (password != null && !password.isBlank()) {
            config.setPassword(password);
        }
        this.nameAPI = new NameAPI(this.logger, new HikariDataSource(config));
    }

    /**
     * Loads the config from disk, and creates it if necessary
     */
    private void loadConfig(Path dataDirectory) {
        try {
            // ensure data directory exists
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
        } catch (IOException e) {
            logger.error("Could not create data directory: {}", dataDirectory, e);
            return;
        }

        // create config file if it doesn't exist
        Path configFile = dataDirectory.resolve("config.yml");
        if (!Files.exists(configFile)) {
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                if (in != null) {
                    Files.copy(in, configFile);
                    logger.info("Default configuration file created.");
                } else {
                    logger.error("Default configuration file is missing in resources!");
                    return;
                }
            } catch (IOException e) {
                logger.error("Could not create default configuration file: {}", configFile, e);
            }
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(configFile).build();
        try {
            config = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not load configuration file: " + configFile, e);
        }
    }
}
