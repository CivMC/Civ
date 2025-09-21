package net.civmc.kitpvp.ranked;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class RankedQueueListener implements Listener {

    private final RankedQueueManager rankedQueueManager;

    public RankedQueueListener(RankedQueueManager rankedQueueManager) {
        this.rankedQueueManager = rankedQueueManager;
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        rankedQueueManager.leaveQueue(player);
        rankedQueueManager.loseMatch(player);
    }

    @EventHandler
    public void on(PlayerDeathEvent event) {
        rankedQueueManager.loseMatch(event.getPlayer());
    }

    @EventHandler
    public void on(PlayerTeleportEvent event) {
        if (event.getTo().getWorld().equals(event.getFrom().getWorld())) {
            return;
        }
        rankedQueueManager.loseMatch(event.getPlayer());
    }
}
