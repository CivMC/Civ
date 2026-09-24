package net.civmc.kitpvp.arena;

import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ArenaGravityListener implements Listener {

    private final ArenaManager manager;
    private final JavaPlugin plugin;

    public ArenaGravityListener(ArenaManager manager, JavaPlugin plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    @EventHandler
    public void on(PlayerChangedWorldEvent event) {
        refresh(event.getPlayer());
    }

    @EventHandler
    public void on(PlayerGameModeChangeEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, () -> refresh(event.getPlayer()));
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        refresh(event.getPlayer());
    }

    @EventHandler
    public void on(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, () -> refresh(event.getPlayer()));
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        resetGravity(event.getPlayer());
    }

    public void refresh(Player player) {
        LoadedArena arena = manager.getArena(player.getWorld().getName());
        if (arena == null || arena.ranked() || player.getGameMode() != GameMode.SURVIVAL) {
            resetGravity(player);
            return;
        }

        setGravity(player, arena.gravity());
        if (!arena.owner().getId().equals(player.getUniqueId())) {
            arena.lockGravity();
        }
    }

    public void resetOnlinePlayers() {
        plugin.getServer().getOnlinePlayers().forEach(this::resetGravity);
    }

    private void resetGravity(Player player) {
        setGravity(player, ArenaGravity.MAIN);
    }

    private void setGravity(Player player, ArenaGravity gravity) {
        AttributeInstance attribute = player.getAttribute(Attribute.GRAVITY);
        if (attribute != null) {
            attribute.setBaseValue(gravity.value());
        }
    }
}
