package com.github.igotyou.FactoryMod.recipes.upgrade;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public interface Upgrade {

    static boolean hasUpgrade(ItemStack[] items) {
        for (ItemStack item : items) {
            if (CustomItem.isCustomItem(item, "factory_upgrade")) {
                return true;
            }
        }
        return false;
    }

    static boolean removeUpgrade(Inventory inventory) {
        ItemStack[] storageContents = inventory.getStorageContents();
        for (int i = 0; i < storageContents.length; i++) {
            ItemStack item = storageContents[i];
            if (CustomItem.isCustomItem(item, "factory_upgrade")) {
                if (item.getAmount() == 1) {
                    inventory.removeItem(item);
                } else {
                    item.setAmount(item.getAmount() - 1);
                }
                return true;
            }
        }
        return false;
    }


}
