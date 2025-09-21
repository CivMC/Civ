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

    private final RankedDao dao;

    private Map<UUID, Double> elo = Collections.emptyMap();

    public RankedPlaceholders(RankedDao dao) {
        this.dao = dao;
        Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
            this.elo = dao.getAll();
        }, 20 * 60 * 10, 20 * 60 * 10);
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
