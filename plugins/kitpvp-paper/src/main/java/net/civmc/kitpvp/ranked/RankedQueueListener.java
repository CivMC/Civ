package net.civmc.kitpvp.ranked;

import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.snapshot.DeathListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class RankedQueueListener implements Listener {

    private final RankedQueueManager rankedQueueManager;
    private final DeathListener deathListener;

    public RankedQueueListener(RankedQueueManager rankedQueueManager, DeathListener deathListener) {
        this.rankedQueueManager = rankedQueueManager;
        this.deathListener = deathListener;
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        rankedQueueManager.leaveQueue(player);
        rankedQueueManager.leaveUnrankedQueue(player);
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
        if (event.getTo().getWorld().getName().startsWith("rankedarena.")) {
            return;
        }
        if (rankedQueueManager.loseMatch(event.getPlayer())) {
            JavaPlugin.getPlugin(KitPvpPlugin.class).info(event.getPlayer().getName() + " forfeited match by teleporting");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void on(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        RankedMatch match = rankedQueueManager.getMatch(player);
        if (match == null) {
            return;
        }
        if (match.opponent().equals(player)) {
            match.addPlayerDamageDealt(event.getFinalDamage());
        } else {
            match.addOpponentDamageDealt(event.getFinalDamage());
        }

        if (player.getHealth() - event.getFinalDamage() < 0) {
            deathListener.die(player);
            event.setCancelled(true);
            rankedQueueManager.loseMatch(player);
        }
    }
}
