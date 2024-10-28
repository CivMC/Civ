package com.github.igotyou.FactoryMod.recipes.upgrade;

import net.civmc.heliodor.meteoriciron.FactoryUpgrade;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface Upgrade {

    static boolean hasUpgrade(ItemStack[] items) {
        for (ItemStack item : items) {
            if (item != null && item.getPersistentDataContainer().has(FactoryUpgrade.FACTORY_UPGRADE_KEY)) {
                return true;
            }
        }
        return false;
    }

    static boolean removeUpgrade(Inventory inventory) {
        ItemStack[] storageContents = inventory.getStorageContents();
        for (int i = 0; i < storageContents.length; i++) {
            ItemStack item = storageContents[i];
            if (item != null && item.getPersistentDataContainer().has(FactoryUpgrade.FACTORY_UPGRADE_KEY)) {
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
