package vg.civcraft.mc.civmodcore.inventory.items.custom;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;
import java.util.List;

public interface CompactedItem {

    NamespacedKey COMPACTED_ITEM_KEY = new NamespacedKey(JavaPlugin.getPlugin(CivModCorePlugin.class), "custom_item");
    Component COMPACTED_ITEM_LORE = Component.text("Compacted Item");

    /**
     * Check if an item is a compacted item
     * @param item The item to check
     * @return If compacted or not
     */
    static boolean isCompactedItem(ItemStack item) {
        if (ItemUtils.isEmptyItem(item)) {
            return false;
        }
        final @Nullable Boolean isCompacted = item.getPersistentDataContainer().get(COMPACTED_ITEM_KEY, PersistentDataType.BOOLEAN);
        return isCompacted != null && isCompacted;
    }

    /**
     * Mark an item as a compacted item
     * @param item The item to mark
     */
    static void markCompactedItem(ItemStack item) {
        if (ItemUtils.isEmptyItem(item)) {
            return;
        }
        // add the pdc marker
        var result = item.editPersistentDataContainer((pdc) -> {
            pdc.set(COMPACTED_ITEM_KEY, PersistentDataType.BOOLEAN, true);
        });
        if (!result) {
            CivModCorePlugin.getInstance().getSLF4JLogger().error("Could not mark {} as a compacted item", item);
            return;
        }
        // add the lore for visual identification
        ItemMeta meta = item.getItemMeta();
        MetaUtils.addComponentLore(meta, COMPACTED_ITEM_LORE);
        item.setItemMeta(meta);
    }

    /**
     * Mark an item as uncompacted (remove the compacted item markers)
     * @param item The item to mark
     */
    static void removeCompactedItemMark(ItemStack item) {
        if (ItemUtils.isEmptyItem(item)) {
            return;
        }
        // mark as uncompacted in pdc
        var result = item.editPersistentDataContainer((pdc) -> {
            pdc.set(COMPACTED_ITEM_KEY, PersistentDataType.BOOLEAN, false);
        });
        if (!result) {
            CivModCorePlugin.getInstance().getSLF4JLogger().error("Could not mark {} as uncompacted item", item);
            return;
        }

        // remove the lore for visual identification
        ItemMeta meta = item.getItemMeta();
        List<Component> lore = MetaUtils.getComponentLore(meta);
        lore.remove(COMPACTED_ITEM_LORE);
        MetaUtils.setComponentLore(meta, lore);
        item.setItemMeta(meta);
    }
}
