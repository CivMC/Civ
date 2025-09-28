package net.civmc.secureboot.paper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

enum PluginStatus {
    AWAITING_STATUS,
    LOADED,
    ENABLED
}

@SuppressWarnings("UnstableApiUsage")
public final class SecureBootPlugin extends JavaPlugin implements Listener {
    private final Map<String, PluginStatus> pluginState = Collections.synchronizedMap(new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
    private final MiniMessage mm = MiniMessage.miniMessage();
    private @NotNull Component kickMessage = Component.text("Server is in maintenance mode.");
    private boolean acceptLogins = false;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        // ensure required plugins have a status
        getConfig().getStringList("required_plugins").forEach((plugin) -> this.pluginState.put(plugin, PluginStatus.AWAITING_STATUS));
        getSLF4JLogger().debug("Awaiting status for: {}", String.join(", ", this.pluginState.keySet()));

        // set kick message from config
        String rawKickMessage = getConfig().getString("kick_message", "Server is in maintenance mode.");
        kickMessage = mm.deserialize(rawKickMessage);
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        // add all plugins loaded by the server
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            this.pluginState.replace(
                plugin.getPluginMeta().getName(),
                PluginStatus.AWAITING_STATUS,
                // Just in case this plugin was enabled prior to SecureBoot
                plugin.isEnabled() ? PluginStatus.ENABLED : PluginStatus.LOADED
            );
        }
    }

    /**
     * This is emitted <i>after</i> the plugin's {@link org.bukkit.plugin.Plugin#onEnable()} method has been called,
     * but not if it threw. This does, however, require plugins to fail exceptionally to be detected as having not
     * started correctly.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void detectPluginEnabled(
        final @NotNull PluginEnableEvent event
    ) {
        // mark enabled plugin as enabled
        this.pluginState.replace(event.getPlugin().getPluginMeta().getName(), PluginStatus.LOADED, PluginStatus.ENABLED);
    }

    /**
     * This is emitted <i>before</i> the plugin's {@link org.bukkit.plugin.Plugin#onDisable()} method is called, so no
     * failure on the plugin's part to properly disable itself should interfere with this.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void detectPluginDisabled(
        final @NotNull PluginDisableEvent event
    ) {
        // mark disabled plugin as loaded
        this.pluginState.replace(event.getPlugin().getPluginMeta().getName(), PluginStatus.ENABLED, PluginStatus.LOADED);
    }

    // need event to run as late as possible to catch any lingering issues
    @EventHandler(priority = EventPriority.MONITOR)
    private void onServerLoad(
        final @NotNull ServerLoadEvent event
    ) {
        var foundError = false;
        for (final Map.Entry<String, PluginStatus> entry : this.pluginState.entrySet()) {
            var pluginName = entry.getKey();
            foundError |= switch (entry.getValue()) {
                case ENABLED -> {
                    getSLF4JLogger().info("{} loaded successfully!", pluginName);
                    yield false;
                }
                case LOADED -> {
                    getSLF4JLogger().warn("{} did not load successfully!", pluginName);
                    yield true; // shutdown
                }
                case AWAITING_STATUS -> {
                    getSLF4JLogger().warn("{} is not present!", pluginName);
                    yield true; // shutdown
                }
            };
        }

        this.acceptLogins = !foundError;
        if (foundError) {
            getSLF4JLogger().warn("Some required plugins did not start as expected; disallowing logins!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerJoin(PlayerJoinEvent event) {
        if (this.acceptLogins) {
            return;
        }

        // notify admins that server is in invalid state
        Player player = event.getPlayer();
        if (player.isOp()) {
            player.sendMessage(mm.deserialize("<red>[Secureboot] Server started in invalid state</red>"));
            return;
        }
        event.getPlayer().kick(kickMessage);
    }
}
