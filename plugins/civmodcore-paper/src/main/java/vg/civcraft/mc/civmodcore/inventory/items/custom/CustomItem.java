package vg.civcraft.mc.civmodcore.inventory.items.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CustomItem {
    public static NamespacedKey CUSTOM_ITEM = new NamespacedKey(JavaPlugin.getPlugin(CivModCorePlugin.class), "custom_item");

    private static final Map<String, ItemStack> customItems = new HashMap<>();

    public static void registerCustomItem(String key, ItemStack item) {
        if (item == null || item.isEmpty()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(CUSTOM_ITEM, PersistentDataType.STRING, key);
        item.setItemMeta(meta);
        if (!customItems.containsKey(key)) {
            customItems.put(key, item.clone());
        }
    }

    public static ItemStack getCustomItem(String key) {
        ItemStack itemStack = customItems.get(key);
        return itemStack == null ? null : itemStack.clone();
    }

    /**
     * Just remember that has-then-get is an anti-pattern: use {@link #isCustomItem(org.bukkit.inventory.ItemStack, String)}
     * or {@link #getCustomItemKey(org.bukkit.inventory.ItemStack)} instead.
     */
    public static boolean isCustomItem(ItemStack item) {
        return item != null && !item.isEmpty() && item.getPersistentDataContainer().has(CUSTOM_ITEM);
    }

    public static boolean isCustomItem(ItemStack item, String key) {
        return item != null && !item.isEmpty() && key.equals(item.getPersistentDataContainer().get(CUSTOM_ITEM, PersistentDataType.STRING));
    }

    public static String getCustomItemKey(ItemStack item) {
        if (item != null && !item.isEmpty()) {
            return item.getPersistentDataContainer().get(CUSTOM_ITEM, PersistentDataType.STRING);
        } else {
            return null;
        }
    }

    public static Set<String> getKeys() {
        return Collections.unmodifiableSet(customItems.keySet());
    }
}
