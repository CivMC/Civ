package net.civmc.kitpvp.warp.util;

import javax.annotation.Nullable;
import org.bukkit.GameMode;

public class EnumUtil {
    public static @Nullable GameMode getGamemode(String s) {
        return switch (s) {
            case "survival" -> GameMode.SURVIVAL;
            case "creative" -> GameMode.CREATIVE;
            case "spectator" -> GameMode.SPECTATOR;
            case "adventure" -> GameMode.ADVENTURE;
            default -> null;
        };
    }
}
