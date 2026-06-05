package net.civmc.zorweth.oxygen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Boat;
import org.bukkit.entity.HappyGhast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ActivityManager implements Listener {

    private static final long ACTIVITY_DURATION_MS = 10_000;

    // last time activity was detected
    private final Map<Player, Map<Activity, Long>> activities = new HashMap<>();

    public Set<Activity> getActivities(Player player) {
        Set<Activity> playerActivities = new HashSet<>();
        Map<Activity, Long> actvs = activities.get(player);
        if (actvs != null) {
            for (Map.Entry<Activity, Long> entry : actvs.entrySet()) {
                if (entry.getValue() + ACTIVITY_DURATION_MS > System.currentTimeMillis()) {
                    playerActivities.add(entry.getKey());
                }
            }
        }

        if (playerActivities.isEmpty()) {
            playerActivities.add(Activity.IDLE);
        }

        return playerActivities;
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        activities.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getVehicle() instanceof Boat || player.getVehicle() instanceof HappyGhast) {
            recordActivity(player, Activity.BOATING);
        } else if (player.isSwimming()) {
            recordActivity(player, Activity.SWIMMING);
        } else if (player.isSprinting()) {
            recordActivity(player, Activity.SPRINTING);
        } else {
            recordActivity(player, Activity.WALKING);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(BlockBreakEvent event) {
        recordActivity(event.getPlayer(), Activity.MINING);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(BlockPlaceEvent event) {
        recordActivity(event.getPlayer(), Activity.MINING);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            recordActivity(player, Activity.COMBAT);
        }
        if (event.getEntity() instanceof Player player) {
            recordActivity(player, Activity.COMBAT);
        }
    }

    // finale overrides this but it works anyway
    @EventHandler(priority = EventPriority.LOWEST)
    public void on(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) {
            return;
        }
        if (player.getPersistentDataContainer().has(OxygenManager.NO_HEALTH_REGEN)) {
            return;
        }

        recordActivity(player, Activity.REGENERATING);
    }

    private void recordActivity(Player player, Activity activity) {
        activities.computeIfAbsent(player, k -> new HashMap<>())
            .put(activity, System.currentTimeMillis());
    }

    public enum Activity {
        IDLE,
        WALKING,
        SPRINTING,
        SWIMMING,
        REGENERATING,
        BOATING,
        MINING,
        COMBAT
    }
}
