package vg.civcraft.mc.civmodcore.utilities;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class to make dealing with namespace keys easier.
 */
public final class KeyedUtils {
    /**
     * Converts a stringified namespaced-key back into a {@link NamespacedKey}.
     */
    public static @Nullable NamespacedKey fromString(
        final String key
    ) {
        return key == null ? null : NamespacedKey.fromString(key);
    }

    /**
     * Converts a {@link Key} into a string.
     *
     * @apiNote This will be immediately defunct the moment we move to Kotlin.
     */
    @Contract("!null -> !null")
    public static @Nullable String getString(
        final Key key
    ) {
        return key == null ? null : key.asString();
    }

    /**
     * Converts a {@link Keyed} into a string.
     *
     * @apiNote This will be immediately defunct the moment we move to Kotlin.
     */
    @Contract("!null -> !null")
    public static @Nullable String getString(
        final Keyed keyed
    ) {
        return keyed == null ? null : getString(keyed.key());
    }

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
