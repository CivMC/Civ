package net.civmc.heliodor.backpack;

import java.util.List;
import net.civmc.heliodor.meteoriciron.MeteoricIron;
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
import vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItemFactory;

public interface Backpack {
    CustomItemFactory BACKPACK = CustomItem.registerCustomItem("backpack", () -> {
        final ItemStack item = ItemStack.of(Material.ENDER_CHEST);
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Backpack", NamedTextColor.YELLOW));
        meta.lore(List.of(
            Component.text("Can be placed and used like an ender chest,", NamedTextColor.WHITE),
            Component.text("but drops its items when you die", NamedTextColor.WHITE),
            Component.text("Cannot contain certain PvP items", NamedTextColor.WHITE)
        ));
        meta.setFireResistant(true);
        item.setItemMeta(meta);
        return item;
    });

    static boolean isBackpack(ItemStack item) {
        return CustomItem.isCustomItem(item, "backpack");
    }

    static List<CraftingRecipe> getRecipes(Plugin plugin) {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "backpack"), BACKPACK.createItem())
            .shape("xxx", "xex", "xxx")
            .setIngredient('x', MeteoricIron.INGOT.createItem())
            .setIngredient('e', Material.ENDER_CHEST);
        recipe.setCategory(CraftingBookCategory.MISC);
        return List.of(recipe);
    }
}
