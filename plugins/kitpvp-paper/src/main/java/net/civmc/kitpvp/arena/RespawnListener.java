package net.civmc.kitpvp.arena;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class RespawnListener implements Listener {

    private static final int INVULNERABILITY_TICKS = 5 * 20;

    private final ArenaManager manager;
    private final JavaPlugin plugin;
    private final Set<UUID> invulnerablePlayers = new HashSet<>();

    public RespawnListener(final ArenaManager manager, final JavaPlugin plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    @EventHandler
    public void on(final PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        final String worldName = world.getName();
        if (manager.isArena(worldName)) {
            for (final LoadedArena arena : manager.getArenas()) {
                if (manager.getArenaName(arena).equals(worldName)) {
                    final Location spawn = arena.arena().spawn().clone();
                    spawn.setWorld(world);
                    event.setRespawnLocation(spawn);
                    grantInvulnerability(player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void on(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (invulnerablePlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void on(final PlayerChangedWorldEvent event) {
        invulnerablePlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        invulnerablePlayers.remove(event.getPlayer().getUniqueId());
    }

    private void grantInvulnerability(final Player player) {
        final UUID uuid = player.getUniqueId();
        invulnerablePlayers.add(uuid);
        Bukkit.getScheduler().runTaskLater(plugin, () -> invulnerablePlayers.remove(uuid), INVULNERABILITY_TICKS);
    }
}
