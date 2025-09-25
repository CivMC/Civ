package net.civmc.kitpvp.ranked;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RankedPlaceholders extends PlaceholderExpansion {

    private RankedPlayers players;

    public RankedPlaceholders(RankedPlayers players) {
        this.players = players;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        params = params.toLowerCase();

        if (params.equalsIgnoreCase("elo")) {
            return Long.toString(Math.round(players.getElo(player.getUniqueId())));
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
