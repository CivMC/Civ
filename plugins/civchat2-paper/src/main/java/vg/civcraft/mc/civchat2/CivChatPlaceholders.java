package vg.civcraft.mc.civchat2;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civchat2.prefix.StarManager;

public class CivChatPlaceholders extends PlaceholderExpansion {
    private final StarManager starManager;

    public CivChatPlaceholders(StarManager starManager) {
        this.starManager = starManager;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        params = params.toLowerCase();

        if (params.equalsIgnoreCase("prefix")) {
            return starManager.getPrefix(player) + ChatColor.RESET;
        } else {
            return null;
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "civchat";
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
