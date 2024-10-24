package net.civmc.heliodor.vein;

import net.civmc.heliodor.HeliodorPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public interface MeteoricIron {

    NamespacedKey NUGGET_KEY = new NamespacedKey(JavaPlugin.getPlugin(HeliodorPlugin.class), "meteoric_icon_nugget");

    static ItemStack createMeteoricIronNugget() {
        ItemStack item = new ItemStack(Material.IRON_NUGGET);
        ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Meteoric Iron Nugget", NamedTextColor.AQUA));
        meta.setEnchantmentGlintOverride(true);
        meta.setFireResistant(true);
        meta.getPersistentDataContainer().set(NUGGET_KEY, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);

        return item;
    }

    static boolean isNugget(ItemStack item) {
        if (item == null || item.getType() != Material.IRON_NUGGET) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(NUGGET_KEY);
    }
}
