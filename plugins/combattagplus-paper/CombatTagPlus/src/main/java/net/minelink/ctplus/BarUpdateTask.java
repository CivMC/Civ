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
        if (!plugin.getSettings().useBarApi()) {
            cancel();
            return;
        }

        Player player = plugin.getPlayer(playerId);
        if (player == null) {
            cancel();
            return;
        }

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

        BarAPI.setMessage(player, YELLOW + "CombatTag: " + WHITE + remaining, percent);
    }

    public static void run(final CombatTagPlus plugin, final Player p) {
        if (!plugin.getSettings().useBarApi()) return;
        if (plugin.getNpcPlayerHelper().isNpc(p)) return;
        if (!Bukkit.getPluginManager().isPluginEnabled("BarAPI")) return;

        final BukkitScheduler s = Bukkit.getScheduler();
        s.scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (!plugin.getTagManager().isTagged(p.getUniqueId()) || !p.isOnline()) {
                    return;
                }

                UUID playerId = p.getUniqueId();
                Integer taskId = tasks.get(playerId);
                if (taskId != null && (s.isQueued(taskId) || s.isCurrentlyRunning(taskId))) {
                    return;
                }

                taskId = new BarUpdateTask(plugin, p).runTaskTimer(plugin, 0, 10).getTaskId();
                tasks.put(playerId, taskId);
            }
        });
    }

    public static void purgeFinished() {
        Iterator<Integer> iterator = tasks.values().iterator();
        BukkitScheduler s = Bukkit.getScheduler();

        while (iterator.hasNext()) {
            int taskId = iterator.next();
            if (!s.isQueued(taskId) && !s.isCurrentlyRunning(taskId)) {
                iterator.remove();
            }
        }
    }

}
