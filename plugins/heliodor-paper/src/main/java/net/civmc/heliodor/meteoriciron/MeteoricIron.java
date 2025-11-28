package net.civmc.heliodor.meteoriciron;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItem;

import java.util.List;
import vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItemFactory;

public interface MeteoricIron {
    CustomItemFactory METEORIC_IRON_NUGGET = CustomItem.registerCustomItem("meteoric_iron_nugget", () -> {
        final ItemStack item = ItemStack.of(Material.IRON_NUGGET);
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Meteoric Iron Nugget", NamedTextColor.AQUA));
        meta.lore(List.of(
            Component.text("A buried fragment from another world", NamedTextColor.WHITE),
            Component.text("Used for its unique magical properties", NamedTextColor.WHITE)
        ));
        meta.setEnchantmentGlintOverride(true);
        meta.setFireResistant(true);
        item.setItemMeta(meta);
        return item;
    });

    CustomItemFactory INGOT = CustomItem.registerCustomItem("meteoric_iron_ingot", () -> {
        final ItemStack item = ItemStack.of(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Meteoric Iron Ingot", NamedTextColor.LIGHT_PURPLE));
        meta.lore(List.of(
            Component.text("A buried fragment from another world", NamedTextColor.WHITE),
            Component.text("Used for its unique magical properties", NamedTextColor.WHITE)
        ));
        meta.setEnchantmentGlintOverride(true);
        meta.setFireResistant(true);
        item.setItemMeta(meta);
        return item;
    });

    static boolean isNugget(ItemStack item) {
        return CustomItem.isCustomItem(item, "meteoric_iron_nugget");
    }

    static boolean isIngot(ItemStack item) {
        return CustomItem.isCustomItem(item, "meteoric_iron_ingot");
    }

    static List<CraftingRecipe> getRecipes(Plugin plugin) {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "meteoric_iron_ingot"), INGOT.createItem())
            .shape("xxx", "xxx", "xxx")
            .setIngredient('x', MeteoricIron.METEORIC_IRON_NUGGET.createItem());
        recipe.setCategory(CraftingBookCategory.MISC);
        return List.of(recipe);
    }
}
