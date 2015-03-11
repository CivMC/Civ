package net.minelink.ctplus;

import me.confuser.barapi.BarAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.ChatColor.*;

public final class BarUpdateTask extends BukkitRunnable {

    private final static Map<UUID, Integer> tasks = new HashMap<>();

    private final CombatTagPlus plugin;

    private final UUID playerId;

    private BarUpdateTask(CombatTagPlus plugin, Player player) {
        this.plugin = plugin;
        this.playerId = player.getUniqueId();
    }

    @Override
    public void run() {
        // Cancel if BarAPI option was disabled
        if (!plugin.getSettings().useBarApi()) {
            cancel();
            return;
        }

        // Cancel if player went offline
        Player player = plugin.getPlayer(playerId);
        if (player == null) {
            cancel();
            return;
        }

        // Remove bar before displaying the next one
        if (BarAPI.hasBar(player)) {
            BarAPI.removeBar(player);
        }

        // Cancel if player is no longer tagged
        Tag tag = plugin.getTagManager().getTag(playerId);
        if (tag == null || tag.isExpired()) {
            BarAPI.setMessage(player, GREEN + "You are no longer in combat!", 1);
            cancel();
            return;
        }

        int remainingDuration = tag.getTagDuration();
        int tagDuration = plugin.getSettings().getTagDuration();
        float percent = ((float) remainingDuration / tagDuration) * 100;
        String remaining = DurationUtils.format(remainingDuration);

        // Display remaining timer in boss bar
        BarAPI.setMessage(player, YELLOW + "CombatTag: " + WHITE + remaining, percent);
    }

    public static void run(final CombatTagPlus plugin, final Player p) {
        // Do nothing if BarAPI option is disabled
        if (!plugin.getSettings().useBarApi()) return;

        // Do nothing if player is a NPC
        if (plugin.getNpcPlayerHelper().isNpc(p)) return;

        // Do nothing if BarAPI isn't even enabled
        if (!Bukkit.getPluginManager().isPluginEnabled("BarAPI")) return;

        final BukkitScheduler s = Bukkit.getScheduler();

        // Schedule the task to run on next tick
        s.scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                // Do nothing if player isn't tagged or online
                if (!plugin.getTagManager().isTagged(p.getUniqueId()) || !p.isOnline()) {
                    return;
                }

                UUID playerId = p.getUniqueId();
                Integer taskId = tasks.get(playerId);

                // Do nothing if player already has an active task
                if (taskId != null && (s.isQueued(taskId) || s.isCurrentlyRunning(taskId))) {
                    return;
                }

                // Create new repeating task
                taskId = new BarUpdateTask(plugin, p).runTaskTimer(plugin, 0, 5).getTaskId();
                tasks.put(playerId, taskId);
            }
        });
    }

    static void purgeFinished() {
        Iterator<Integer> iterator = tasks.values().iterator();
        BukkitScheduler s = Bukkit.getScheduler();

        // Loop over each task
        while (iterator.hasNext()) {
            int taskId = iterator.next();

            // Remove entry if task isn't running anymore
            if (!s.isQueued(taskId) && !s.isCurrentlyRunning(taskId)) {
                iterator.remove();
            }
        }
    }

}
