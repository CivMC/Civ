package net.civmc.zorweth.research;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.civmc.zorweth.ZorwethPlugin;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;

public final class ResearchDisplay implements Listener {

    private final BooleanSetting showResearchProgress;
    private final ResearchManager researchManager;
    private final Map<UUID, BossBar> bars = new HashMap<>();
    private final BukkitTask task;

    public ResearchDisplay(final ZorwethPlugin plugin, final ResearchManager researchManager) {
        this.researchManager = researchManager;
        this.showResearchProgress = new BooleanSetting(plugin, true, "Show research progress boss bar",
            "showResearchProgressBossBar", "Should current research progress be shown in a boss bar?");
        plugin.getSettingsMenu().registerSetting(this.showResearchProgress);
        this.showResearchProgress.registerListener((playerId, setting, oldValue, newValue) -> {
            final Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                return;
            }
            if (Boolean.TRUE.equals(newValue)) {
                updateBossBar(player);
            } else {
                removeBossBar(player);
            }
        });
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                updateBossBar(player);
            }
        }, 40L, 40L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        updateBossBar(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        removeBossBar(event.getPlayer());
    }

    public void disable() {
        this.task.cancel();
        for (final BossBar bar : this.bars.values()) {
            bar.removeAll();
        }
        this.bars.clear();
    }

    private void updateBossBar(final Player player) {
        final ResearchProgress progress = this.researchManager.getResearchProgress();
        if (progress.complete() || !this.showResearchProgress.getValue(player.getUniqueId())) {
            removeBossBar(player);
            return;
        }

        final BossBar bar = this.bars.computeIfAbsent(player.getUniqueId(), ignored -> {
            final BossBar created = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SEGMENTED_10);
            created.addPlayer(player);
            return created;
        });
        bar.setTitle("Research phase " + progress.phase() + ": " + progress.runs() + "/" + progress.requiredRuns());
        bar.setProgress(Math.clamp((double) progress.runs() / (double) progress.requiredRuns(), 0.0d, 1.0d));
        bar.setVisible(true);
    }

    private void removeBossBar(final Player player) {
        final BossBar bar = this.bars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }
}
