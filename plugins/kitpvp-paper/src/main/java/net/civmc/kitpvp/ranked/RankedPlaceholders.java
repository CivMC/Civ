package net.civmc.kitpvp.ranked;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.civmc.kitpvp.KitPvpPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RankedPlaceholders extends PlaceholderExpansion {

    private Map<UUID, Double> elo;

    public RankedPlaceholders(RankedDao dao) {
        this.elo = dao.getAll();
        Bukkit.getScheduler().runTaskTimerAsynchronously(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
            Map<UUID, Double> all = dao.getAll();
            Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
                this.elo = all;
            });
            this.elo = all;
        }, 20 * 60 * 5, 20 * 60 * 5);
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        params = params.toLowerCase();

        if (params.equalsIgnoreCase("elo")) {
            return Long.toString(Math.round(elo.getOrDefault(player.getUniqueId(), 1000D)));
        } else {
            return null;
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "kitpvp";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Okx";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
       return true;
    }
}
