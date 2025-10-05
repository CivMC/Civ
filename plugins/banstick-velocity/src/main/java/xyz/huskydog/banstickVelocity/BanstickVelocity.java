package xyz.huskydog.banstickVelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import xyz.huskydog.banstickCore.BanstickCore;
import xyz.huskydog.banstickCore.BanstickPlugin;
import java.nio.file.Path;

@Plugin(id = "banstick-velocity",
    name = "banstick-velocity", version = "1.0.0",
    authors = {"Huskydog9988"})
public class BanstickVelocity implements BanstickPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public BanstickVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Initializing Banstick Velocity plugin");

        // Initialize BanstickCore
        BanstickCore banstickCore = new BanstickCore(this);

        // Example usage: Log a config value
        var config = banstickCore.getConfig().getRawConfig();
        String exampleValue = config.node("database").node("host").getString();
        logger.info("Config value for 'database.host': {}", exampleValue);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Path getDataDirectory() {
        return dataDirectory;
    }
}
