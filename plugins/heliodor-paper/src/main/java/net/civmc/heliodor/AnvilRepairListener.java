package net.civmc.heliodor;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class AnvilRepairListener implements Listener {

    private static final NamespacedKey NO_COMBINE = new NamespacedKey(JavaPlugin.getPlugin(HeliodorPlugin.class), "no_combine");

    @EventHandler
    public void on(PrepareResultEvent event) {
        if (!(event.getInventory() instanceof AnvilInventory inventory)) {
            return;
        }

        ItemStack firstItem = inventory.getFirstItem();
        ItemStack secondItem = inventory.getSecondItem();
        if (firstItem != null && firstItem.getPersistentDataContainer().has(NO_COMBINE)) {
            if (secondItem != null && !secondItem.isEmpty()) {
                event.setResult(null);
            }
        } else if (secondItem != null && secondItem.getPersistentDataContainer().has(NO_COMBINE)) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void on(PrepareItemEnchantEvent event) {
        ItemStack item = event.getItem();
        if (item.getPersistentDataContainer().has(NO_COMBINE)) {
            event.setCancelled(true);
        }
    }

    public static void setNoCombine(ItemMeta meta) {
        meta.getPersistentDataContainer().set(NO_COMBINE, PersistentDataType.BOOLEAN, true);
    }
}
