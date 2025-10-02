package vg.civcraft.mc.civmodcore.inventory.items.custom;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

/// Since Minecraft doesn't \[yet] offer a means of registering custom item materials, this is the intended means of
/// defining custom items in the meantime. Keep in mind that each custom item must correlate 1:1 with its key, ie, that
/// custom-item keys should be treated like item materials. Do NOT use custom-item keys as custom-item categories, such
/// as compacted items. You must always be able to receive the same item from the same key.
public final class CustomItem {
    public static NamespacedKey CUSTOM_ITEM_KEY = new NamespacedKey(JavaPlugin.getPlugin(CivModCorePlugin.class), "custom_item");

    private static final Map<String, CustomItemFactory> customItems = new ConcurrentHashMap<>();

    public static @NotNull CustomItemFactory registerCustomItem(
        final @NotNull String customKey,
        final @NotNull CustomItemFactory factory
    ) {
        Objects.requireNonNull(customKey);
        final ItemStack template = Objects.requireNonNull(factory.createItem());
        setCustomItemKey(template, customKey);
        setCustomItemModel(template, customKey);
        customItems.putIfAbsent(
            Objects.requireNonNull(customKey),
            template::clone
        );
        return template::clone;
    }

    /// @deprecated Please use [#registerCustomItem(String, CustomItemFactory)] instead!
    @Deprecated(forRemoval = true)
    public static @NotNull CustomItemFactory registerCustomItem(
        final @NotNull String customKey,
        final @NotNull ItemStack template
    ) {
        return registerCustomItem(customKey, template::clone);
    }

    public static @Nullable ItemStack getCustomItem(
        final @NotNull String customKey
    ) {
        final CustomItemFactory factory = customItems.get(Objects.requireNonNull(customKey));
        if (factory == null) {
            return null;
        }
        return factory.createItem();
    }

    /// Just remember that has-then-get is an antipattern! Use [#isCustomItem(org.bukkit.inventory.ItemStack,String)]
    /// or [#getCustomItemKey(org.bukkit.inventory.ItemStack)] instead.
    public static boolean isCustomItem(
        final ItemStack item
    ) {
        return getCustomItemKey(item) != null;
    }

    public static boolean isCustomItem(
        final ItemStack item,
        final @NotNull String customKey
    ) {
        return customKey.equals(getCustomItemKey(item));
    }

    /// @apiNote Even if the item contains a custom-item key, this will return null if that key is not registered!
    public static @Nullable String getCustomItemKey(
        final ItemStack item
    ) {
        if (ItemUtils.isEmptyItem(item)) {
            return null;
        }
        final String key = item.getPersistentDataContainer().get(CUSTOM_ITEM_KEY, PersistentDataType.STRING);
        if (!customItems.containsKey(key)) {
            return null;
        }
        return key;
    }

    /// Set custom item key in pdc to mark item as a custom civ item
    @ApiStatus.Internal
    public static void setCustomItemKey(
        final @NotNull ItemStack item,
        final @NotNull String customKey
    ) {
        Objects.requireNonNull(customKey);
        item.editPersistentDataContainer((pdc) -> pdc.set(CUSTOM_ITEM_KEY, PersistentDataType.STRING, customKey));
    }

    /// Adds a "civ:"-prefixed version of the given key to the item's custom model data string set.
    @ApiStatus.Internal
    @SuppressWarnings("UnstableApiUsage")
	public static void setCustomItemModel(
        final @NotNull ItemStack item,
        @NotNull String customKey
    ) {
        customKey = "civ:" + Objects.requireNonNull(customKey);
        // Java *really* needs https://openjdk.org/jeps/468, these builder patterns are so unergonomic!
        final CustomModelData.Builder builder;
        if (item.getData(DataComponentTypes.CUSTOM_MODEL_DATA) instanceof final CustomModelData existing) {
            final List<String> strings = existing.strings();
            if (strings.contains(customKey)) {
                return; // it already includes the custom-item key!
            }
            builder = CustomModelData.customModelData()
                .addFloats(existing.floats())
                .addFlags(existing.flags())
                .addStrings(existing.strings())
                .addColors(existing.colors());
        }
        else {
            builder = CustomModelData.customModelData();
        }
        builder.addString(customKey);
        item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, builder.build());
    }

    public static @NotNull @UnmodifiableView Set<@NotNull String> getRegisteredKeys() {
        return Collections.unmodifiableSet(customItems.keySet());
    }
}
