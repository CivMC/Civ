package vg.civcraft.mc.civmodcore.utilities;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class to make dealing with namespace keys easier.
 */
public final class KeyedUtils {
    /**
     * Convenience method that lets you construct a key namespaced to a plugin without needing a plugin instance.
     *
     * <pre><code>
     * // If you used NamespacedKey as intended
     * new NamespacedKey(ExamplePlugin.getInstance(), "example");
     *
     * // Using KeyedUtils.pluginKey()
     * KeyedUtils.pluginKey(ExamplePlugin.class, "example");
     * </code></pre>
     */
    public static <T extends JavaPlugin> @NotNull NamespacedKey pluginKey(
        final @NotNull Class<T> pluginClass,
        final @NotNull String key
    ) {
        return new NamespacedKey(JavaPlugin.getPlugin(pluginClass), key);
    }

    /**
     * Creates a new {@link NamespacedKey} for testing purposes.
     */
    public static @NotNull NamespacedKey testKey(
        final @NotNull String key
    ) {
        return new NamespacedKey("test", key);
    }

    /**
     * Creates a <a href="https://dictionary.cambridge.org/dictionary/english/ditto">ditto</a> key, primarily for child
     * keys within {@link org.bukkit.persistence.PersistentDataContainer PersistentDataContainers}.
     *
     * <pre><code>
     * // If you used PDCs as intended
     * {
     *     "CivModCore:players": [
     *         {
     *             "CivModCore:name": "Example",
     *             "CivModCore:uuid": "61629d9c-c2a1-4379-b98e-4b538c6628c5"
     *         }
     *     ]
     * }
     *
     * // Using ditto keys
     * {
     *     "CivModCore:players": [
     *         {
     *             ".:name": "Example",
     *             ".:uuid": "61629d9c-c2a1-4379-b98e-4b538c6628c5"
     *         }
     *     ]
     * }
     * </code></pre>
     */
    public static @NotNull NamespacedKey dittoKey(
        final @NotNull String key
    ) {
        return new NamespacedKey(".", key);
    }
}
