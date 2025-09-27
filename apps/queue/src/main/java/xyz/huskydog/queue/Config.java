package xyz.huskydog.queue;

import java.util.List;
import java.util.Optional;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public final class Config {
    public static final String HOST = System.getProperty("host", "0.0.0.0");
    public static final int PORT = parseInt("port", 25565);
    public static final boolean HIDE_PLAYERS = parseBool("hidePlayers", true);
    public static final boolean DISABLE_CHAT = parseBool("disableChat", true);
    // public static final boolean PLAY_XP = parseBool("playXP", true);
    public static final String PROXY = System.getProperty("proxy", "NONE");

    // ############################################################
    // Property Parsers
    // ############################################################

    public static boolean parseBool(
        final @NotNull String name,
        final boolean defaultValue
    ) {
        return Boolean.parseBoolean(System.getProperty(name, Boolean.toString(defaultValue)));
    }

    public static int parseInt(
        final @NotNull String name,
        final int defaultValue
    ) {
        // Yes, I do know about Integer.getInteger() but it'll silently fallback to the defaultValue if the found value
        // is not correctly formatted as a number. I'd prefer to make the error explicit to the user.
        final String property = System.getProperty(name);
        if (property == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(property);
        }
        catch (final NumberFormatException ignored) {
            MinecraftServer.LOGGER.warn("Could not parse config int [name: {}] [value: {}], defaulting to: {}", name, property, defaultValue);
            return defaultValue;
        }
    }

}
