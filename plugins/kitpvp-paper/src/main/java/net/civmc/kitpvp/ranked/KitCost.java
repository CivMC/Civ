package net.civmc.kitpvp.ranked;

import net.civmc.kitpvp.kit.KitCustomItem;
import net.civmc.kitpvp.kit.KitItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitCost {

    public static final int MAX_POINTS = 300;

    public static final Map<Enchantment, Integer> ENCHANTMENT_COST_PER_LEVEL = new HashMap<>();

    static {
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.PROTECTION, 4);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.PROJECTILE_PROTECTION, 1);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.BLAST_PROTECTION, 2);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.FIRE_PROTECTION, 1);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.SHARPNESS, 1);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.FIRE_ASPECT, 1);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.KNOCKBACK, 1);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.POWER, 1);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.PUNCH, 1);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.FLAME, 1);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.AQUA_AFFINITY, 1);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.RESPIRATION, 1);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.FEATHER_FALLING, 1);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.DEPTH_STRIDER, 1);
    }

    public static ItemStack setPoints(ItemStack item, int points) {
        ItemStack cloned = item.clone();
        ItemMeta clonedMeta = cloned.getItemMeta();
        List<Component> lore = clonedMeta.hasLore() ? clonedMeta.lore() : new ArrayList<>();
        lore.add(0, Component.text(points + " point" + (points == 1 ? "" : "s"), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        clonedMeta.lore(lore);
        cloned.setItemMeta(clonedMeta);
        return cloned;
    }

    public static int getCost(ItemStack[] items) {
        int cost = 0;
        for (ItemStack item : items) {
            cost += getCost(item);
        }
        return cost;
    }

    public static int getCost(ItemStack item) {
        if (item.isEmpty()) {
            return 0;
        }

        String key = CustomItem.getCustomItemKey(item);
        if (key != null) {
            for (KitCustomItem customItem : KitCustomItem.values()) {
                if (customItem.getItem().equals(key)) {
                    return customItem.getCost();
                }
            }
        }

        int cost = 0;

        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            cost += ENCHANTMENT_COST_PER_LEVEL.getOrDefault(entry.getKey(), 0) * entry.getValue();
        }

        for (KitItem kitItem : KitItem.values()) {
            if (item.getType() == kitItem.getItem()) {
                cost += kitItem.getCost();
                break;
            }
        }

        if (item.getItemMeta().isUnbreakable()) {
            cost += 100;
        }

        return cost;
    }
}
