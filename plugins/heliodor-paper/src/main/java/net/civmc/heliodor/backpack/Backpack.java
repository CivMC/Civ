package net.civmc.heliodor.backpack;

import net.civmc.heliodor.HeliodorPlugin;
import net.civmc.heliodor.vein.MeteoricIron;
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
import java.util.List;

public interface Backpack {

    NamespacedKey BACKPACK_KEY = new NamespacedKey(JavaPlugin.getPlugin(HeliodorPlugin.class), "backpack");

    static ItemStack createBackpack() {
        ItemStack item = new ItemStack(Material.ENDER_CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Backpack", NamedTextColor.YELLOW));
        meta.lore(List.of(Component.text("Can be placed and used like an ender chest,", NamedTextColor.WHITE),
            Component.text("but drops its items when you die", NamedTextColor.WHITE),
            Component.text("Cannot contain certain PvP items", NamedTextColor.WHITE)));
        meta.setFireResistant(true);
        meta.getPersistentDataContainer().set(BACKPACK_KEY, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);

        return item;
    }

    static boolean isBackpack(ItemStack item) {
        if (item == null || item.getType() != Material.ENDER_CHEST) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(BACKPACK_KEY);
    }

    static List<ShapedRecipe> getRecipes(Plugin plugin) {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "backpack"), Backpack.createBackpack())
            .shape("xxx", "xex", "xxx")
            .setIngredient('x', MeteoricIron.createMeteoricIronIngot())
            .setIngredient('e', Material.ENDER_CHEST);
        recipe.setCategory(CraftingBookCategory.MISC);
        return List.of(recipe);
    }
}
