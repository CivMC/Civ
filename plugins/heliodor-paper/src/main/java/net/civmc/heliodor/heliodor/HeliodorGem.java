package net.civmc.heliodor.heliodor;

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

public interface HeliodorGem {

    NamespacedKey FINISHED_KEY = new NamespacedKey(JavaPlugin.getPlugin(HeliodorPlugin.class), "finished");
    NamespacedKey CHARGE_KEY = new NamespacedKey(JavaPlugin.getPlugin(HeliodorPlugin.class), "charge");
    NamespacedKey MAX_CHARGE_KEY = new NamespacedKey(JavaPlugin.getPlugin(HeliodorPlugin.class), "max_charge");

    static ItemStack createHeliodorGem(int charge, int maxCharge) {
        if (charge < 0 || charge > 100) {
            throw new IllegalArgumentException("0 <= charge <= 100 for: " + charge);
        } else if (maxCharge < 0 || maxCharge > 100) {
            throw new IllegalArgumentException("0 <= maxCharge <= 100 for: " + maxCharge);
        } else if (maxCharge < charge) {
            throw new IllegalArgumentException("maxCharge >= charge for: " + maxCharge + " >= " + charge);
        }
        ItemStack item = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Rough Heliodor Gem (" + charge + "% infused)", NamedTextColor.AQUA));
        meta.setEnchantmentGlintOverride(true);
        meta.setFireResistant(true);
        meta.lore(List.of(
            Component.text("Can be infused " + (maxCharge - charge) + "% more before needing to be refilled", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
            Component.text("Place above a cauldron filled with lava to infuse", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(CHARGE_KEY, PersistentDataType.INTEGER, charge);
        meta.getPersistentDataContainer().set(MAX_CHARGE_KEY, PersistentDataType.INTEGER, maxCharge);
        item.setItemMeta(meta);

        return item;
    }

    static ItemStack createFinishedHeliodorGem() {
        ItemStack item = new ItemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Heliodor Gem", NamedTextColor.LIGHT_PURPLE));
        meta.lore(List.of(
            Component.text("Seems to have special magnetic properties", NamedTextColor.WHITE),
            Component.text("Can be crafted into a pickaxe", NamedTextColor.WHITE)
        ));
        meta.setEnchantmentGlintOverride(true);
        meta.setFireResistant(true);
        meta.getPersistentDataContainer().set(FINISHED_KEY, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);

        return item;
    }

    static Integer getCharge(ItemStack item) {
        if (item == null || item.getType() != Material.GOLD_BLOCK) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(CHARGE_KEY, PersistentDataType.INTEGER);
    }

    static Integer getMaxCharge(ItemStack item) {
        if (item == null || item.getType() != Material.GOLD_BLOCK) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(MAX_CHARGE_KEY, PersistentDataType.INTEGER);
    }

    static boolean isFinished(ItemStack item) {
        if (item == null || item.getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(FINISHED_KEY);
    }
}
