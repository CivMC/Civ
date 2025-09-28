package net.civmc.kitpvp.arena;

import net.civmc.kitpvp.spawn.SpawnProvider;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class PrivateArenaListener implements Listener {

    private final SpawnProvider provider;
    private final ArenaManager manager;

    public PrivateArenaListener(SpawnProvider provider, ArenaManager manager) {
        this.provider = provider;
        this.manager = manager;
    }

    @EventHandler
    public void on(PlayerSpawnLocationEvent event) {
        // if someone joins an arena they are not invited to, remove them
        for (LoadedArena arena : manager.getArenas()) {
            if (shouldRemove(arena, event.getPlayer())) {
                Location spawn = provider.getSpawn();
                if (spawn != null) {
                    event.setSpawnLocation(spawn);
                }
            }
        }
    }

    private boolean shouldRemove(LoadedArena arena, Player player) {
        return arena.invitedPlayers() != null
            && !arena.invitedPlayers().contains(player.getPlayerProfile())
            && !player.hasPermission("kitpvp.admin")
            && manager.getArenaName(arena).equals(player.getWorld().getName());
    }

    public void remove(LoadedArena arena, Player player) {
        if (shouldRemove(arena, player)) {
            Location spawn = provider.getSpawn();
            if (spawn != null) {
                player.teleport(spawn);
            } else {
                player.kick(Component.text("You have been removed from this arena"));
            }
        }
    }
}
