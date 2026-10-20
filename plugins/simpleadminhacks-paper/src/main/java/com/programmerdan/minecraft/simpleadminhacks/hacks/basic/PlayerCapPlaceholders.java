package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlayerCapPlaceholders extends PlaceholderExpansion {

    private final PlayerCap playerCap;

    public PlayerCapPlaceholders(@NotNull final PlayerCap playerCap) {
        this.playerCap = playerCap;
    }

    @Override
    public @Nullable String onPlaceholderRequest(@Nullable final Player player, @NotNull final String params) {
        return params.equalsIgnoreCase("tps")
            ? new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(playerCap.getTps()) : null;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "playercap";
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
}
