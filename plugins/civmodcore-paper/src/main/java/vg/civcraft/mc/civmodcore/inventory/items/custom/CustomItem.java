package vg.civcraft.mc.civmodcore.inventory.items.custom;

import java.util.Objects;
import java.util.function.Supplier;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

/**
 * Since Minecraft doesn't [yet] offer a means of registering custom item materials, this is the intended means of
 * defining custom items in the meantime. Keep in mind that each custom item must correlate 1:1 with its key, ie, that
 * custom-item keys should be treated like item materials. Do NOT use custom-item keys as custom-item categories, such
 * as compacted items. You must always be able to receive the same item from the same key.
 */
public final class CustomItem {
    public static NamespacedKey CUSTOM_ITEM_KEY = new NamespacedKey(JavaPlugin.getPlugin(CivModCorePlugin.class), "custom_item");

    private static final Map<String, Supplier<ItemStack>> customItems = new HashMap<>();

    public static void registerCustomItem(
        final @NotNull String key,
        final @NotNull Supplier<@NotNull ItemStack> factory
    ) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(factory);
        customItems.putIfAbsent(key, factory);
    }

    public static void registerCustomItem(
        final @NotNull String key,
        final @NotNull ItemStack template
    ) {
        registerCustomItem(key, template::clone);
    }

    public static @Nullable ItemStack getCustomItem(
        final @NotNull String key
    ) {
        if (customItems.get(Objects.requireNonNull(key)) instanceof final Supplier<ItemStack> factory) {
            final ItemStack item = factory.get();
            setCustomItemKey(item, key);
            return item;
        }
        return null;
    }

    /**
     * Just remember that has-then-get is an anti-pattern: use {@link #isCustomItem(org.bukkit.inventory.ItemStack, String)}
     * or {@link #getCustomItemKey(org.bukkit.inventory.ItemStack)} instead.
     */
    public static boolean isCustomItem(
        final ItemStack item
    ) {
        return !ItemUtils.isEmptyItem(item) && item.getPersistentDataContainer().has(CUSTOM_ITEM_KEY);
    }

    public static boolean isCustomItem(
        final ItemStack item,
        final @NotNull String key
    ) {
        return key.equals(getCustomItemKey(item));
    }

    public static @Nullable String getCustomItemKey(
        final ItemStack item
    ) {
        if (!ItemUtils.isEmptyItem(item)) {
            return item.getPersistentDataContainer().get(CUSTOM_ITEM_KEY, PersistentDataType.STRING);
        }
        return null;
    }

    @ApiStatus.Internal
    public static void setCustomItemKey(
        final @NotNull ItemStack item,
        final @NotNull String key
    ) {
        item.editPersistentDataContainer((pdc) -> pdc.set(CUSTOM_ITEM_KEY, PersistentDataType.STRING, key));
    }

    public static @NotNull Set<@NotNull String> getRegisteredKeys() {
        return Collections.unmodifiableSet(customItems.keySet());
    }
}
