package net.civmc.heliodor.backpack;

import java.util.List;
import net.civmc.heliodor.meteoriciron.MeteoricIron;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public interface Backpack {

    static ItemStack createBackpack() {
        ItemStack item = new ItemStack(Material.ENDER_CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Backpack", NamedTextColor.YELLOW));
        meta.lore(List.of(Component.text("Can be placed and used like an ender chest,", NamedTextColor.WHITE),
            Component.text("but drops its items when you die", NamedTextColor.WHITE),
            Component.text("Cannot contain certain PvP items", NamedTextColor.WHITE)));
        meta.setFireResistant(true);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem("backpack", item);

        return item;
    }

    static boolean isBackpack(ItemStack item) {
        return CustomItem.isCustomItem(item, "backpack");
    }

    static List<ShapedRecipe> getRecipes(Plugin plugin) {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "backpack"), Backpack.createBackpack())
            .shape("xxx", "xex", "xxx")
            .setIngredient('x', MeteoricIron.createIngot())
            .setIngredient('e', Material.ENDER_CHEST);
        recipe.setCategory(CraftingBookCategory.MISC);
        return List.of(recipe);
    }
}
