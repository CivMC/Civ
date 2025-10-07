package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import java.util.Map;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerCountPlaceholders extends PlaceholderExpansion {

    private Map<String, Integer> pings;

    public PlayerCountPlaceholders(Map<String, Integer> pings) {
        this.pings = pings;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        params = params.toLowerCase();

        return Integer.toString(pings.getOrDefault(params.toLowerCase(), 0));
    }

    @Override
    public @NotNull String getIdentifier() {
        return "playercount";
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
