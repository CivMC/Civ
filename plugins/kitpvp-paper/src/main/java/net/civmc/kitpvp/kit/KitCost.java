package net.civmc.kitpvp.kit;

import com.dre.brewery.Brew;
import com.dre.brewery.recipe.BRecipe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public class KitCost {

    public static final int MAX_POINTS = 50;

    public static final Map<Enchantment, Integer> ENCHANTMENT_COST_PER_LEVEL = new HashMap<>();

    static {
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.PROTECTION, 1);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.SHARPNESS, 1);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.KNOCKBACK, 1);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.POWER, 1);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.THORNS, 1);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.SWIFT_SNEAK, 100);
        ENCHANTMENT_COST_PER_LEVEL.put(Enchantment.PIERCING, 1);
    }

    public static ItemStack setPoints(ItemStack item, int points) {
        ItemStack cloned = item.clone();
        if (points == 0) {
            return cloned;
        }
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

        if (item.hasItemMeta() && item.getItemMeta() instanceof PotionMeta meta) {
            if (item.getType() == Material.TIPPED_ARROW) {
                cost += 4;
            }
            for (KitPotion potion : KitPotion.values()) {
                if (meta.getBasePotionType() == potion.getType()) {
                    cost += potion.getCost();
                }
            }
        }

        if (Bukkit.getPluginManager().isPluginEnabled("BreweryX")) {
            Brew brew = Brew.get(item);
            if (brew != null) {
                for (KitDrugs drugs : KitDrugs.values()) {
                    BRecipe recipe = BRecipe.getMatching(drugs.getBrew());
                    if (recipe != null && recipe.getRecipeName().equals(brew.getCurrentRecipe().getRecipeName())) {
                        cost += drugs.getCost();
                    }
                }
            }
        }

        if (item.getItemMeta().isUnbreakable()) {
            cost += 100;
        }

        return cost;
    }
}
