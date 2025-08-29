package com.untamedears.realisticbiomes.breaker;

import com.untamedears.realisticbiomes.RealisticBiomes;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;

public class HandPicked {
    private static final NamespacedKey KEY = new NamespacedKey(JavaPlugin.getPlugin(RealisticBiomes.class), "hand_picked");

    public static ItemStack markHandPicked(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(
            Component.empty().append(Component.text("Hand picked")),
            Component.empty().append(Component.text("Unbruised and suitable for use in XP production"))
        ));
        meta.getPersistentDataContainer().set(KEY, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isHandPicked(ItemStack item) {
        return item.getPersistentDataContainer().has(KEY);
    }

    public static ItemStack removeHandPicked(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().remove(KEY);
        item.setItemMeta(meta);
        return item;
    }
}
