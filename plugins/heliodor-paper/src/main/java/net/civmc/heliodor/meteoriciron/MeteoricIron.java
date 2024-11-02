package net.civmc.heliodor.meteoriciron;

import net.civmc.heliodor.HeliodorPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

import java.util.List;

public interface MeteoricIron {

    static ItemStack createMeteoricIronNugget() {
        ItemStack item = new ItemStack(Material.IRON_NUGGET);
        ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Meteoric Iron Nugget", NamedTextColor.AQUA));
        meta.lore(List.of(Component.text("A buried fragment from another world", NamedTextColor.WHITE),
            Component.text("Used for its unique magical properties", NamedTextColor.WHITE)));
        meta.setEnchantmentGlintOverride(true);
        meta.setFireResistant(true);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem("meteoric_iron_nugget", item);

        return item;
    }

    static ItemStack createIngot() {
        ItemStack item = new ItemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
        ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Meteoric Iron Ingot", NamedTextColor.LIGHT_PURPLE));
        meta.lore(List.of(Component.text("A buried fragment from another world", NamedTextColor.WHITE),
            Component.text("Used for its unique magical properties", NamedTextColor.WHITE)));
        meta.setEnchantmentGlintOverride(true);
        meta.setFireResistant(true);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem("meteoric_iron_ingot", item);

        return item;
    }


    static boolean isNugget(ItemStack item) {
        return CustomItem.isCustomItem(item, "meteoric_iron_nugget");
    }

    static boolean isIngot(ItemStack item) {
        return CustomItem.isCustomItem(item, "meteoric_iron_ingot");
    }

    static List<ShapedRecipe> getRecipes(Plugin plugin) {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "meteoric_iron_ingot"), MeteoricIron.createIngot())
            .shape("xxx", "xxx", "xxx")
            .setIngredient('x', MeteoricIron.createMeteoricIronNugget());
        recipe.setCategory(CraftingBookCategory.MISC);
        return List.of(recipe);
    }
}
