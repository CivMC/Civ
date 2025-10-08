package xyz.huskydog.banstickVelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import xyz.huskydog.banstickCore.BanstickCore;
import xyz.huskydog.banstickCore.BanstickPlugin;
import xyz.huskydog.banstickCore.Config;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Plugin(id = "banstick-velocity",
    name = "banstick-velocity", version = "1.0.0",
    authors = {"Huskydog9988"})
public class BanstickVelocity implements BanstickPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Config config;

    @Inject
    public BanstickVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.config = new Config(this);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Initializing Banstick Velocity plugin");

        // Initialize BanstickCore
        BanstickCore banstickCore = new BanstickCore(this);

        // Example usage: Log a config value
        String exampleValue = config.getRawConfig().node("database").node("host").getString();
        logger.info("Config value for 'database.host': {}", exampleValue);
    }

    @Override
    public @NotNull Logger getLogger() {
        return this.logger;
    }

    @Override
    public @NotNull Path getDataDirectory() {
        return this.dataDirectory;
    }

    @Override
    public @NotNull Config getConfig() {
        return this.config;
    }

    public @NotNull String getPluginId() {
        Plugin annotation = this.getClass().getAnnotation(Plugin.class);
        if (annotation == null) {
            throw new IllegalStateException("Plugin annotation is missing, cannot determine plugin ID.");
        }

        return annotation.id();
    }

    @Override
    public void scheduleAsyncTask(@NotNull Runnable task, long delay, long period) {
        server.getScheduler().buildTask(this, task)
            .delay(delay, TimeUnit.SECONDS)
            .repeat(period, TimeUnit.SECONDS)
            .schedule();
    }

    @Override
    public void scheduleAsyncTask(@NotNull Runnable task, long delay) {
        server.getScheduler().buildTask(this, task)
            .delay(delay, TimeUnit.SECONDS)
            .schedule();
    }

    @Override
    public boolean kickPlayer(@NotNull UUID uuid, @NotNull Component reason) {
        Optional<Player> player = server.getPlayer(uuid);
        if (player.isPresent()) {
            player.get().disconnect(reason);
            return true;
        }
        return false;
    }

    @Override
    public void broadcastMessage(@NotNull Component message, @NotNull String permission) {
        server.getAllPlayers().stream()
            .filter(p -> p.hasPermission(permission))
            .forEach(p -> p.sendMessage(message));
    }
}
