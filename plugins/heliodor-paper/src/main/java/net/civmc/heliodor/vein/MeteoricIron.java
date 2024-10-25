package net.civmc.heliodor.vein;

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

import java.util.List;

public interface MeteoricIron {

    NamespacedKey NUGGET_KEY = new NamespacedKey(JavaPlugin.getPlugin(HeliodorPlugin.class), "meteoric_iron_nugget");
    NamespacedKey INGOT_KEY = new NamespacedKey(JavaPlugin.getPlugin(HeliodorPlugin.class), "meteoric_iron_ingot");

    static ItemStack createMeteoricIronNugget() {
        ItemStack item = new ItemStack(Material.IRON_NUGGET);
        ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Meteoric Iron Nugget", NamedTextColor.AQUA));
        meta.lore(List.of(Component.text("A buried fragment from another world", NamedTextColor.WHITE),
            Component.text("Used for its unique magical properties", NamedTextColor.WHITE)));
        meta.setEnchantmentGlintOverride(true);
        meta.setFireResistant(true);
        meta.getPersistentDataContainer().set(NUGGET_KEY, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);

        return item;
    }

    static ItemStack createMeteoricIronIngot() {
        ItemStack item = new ItemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
        ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Meteoric Iron Ingot", NamedTextColor.LIGHT_PURPLE));
        meta.lore(List.of(Component.text("A buried fragment from another world", NamedTextColor.WHITE),
            Component.text("Used for its unique magical properties", NamedTextColor.WHITE)));
        meta.setEnchantmentGlintOverride(true);
        meta.setFireResistant(true);
        meta.getPersistentDataContainer().set(INGOT_KEY, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);

        return item;
    }


    static boolean isNugget(ItemStack item) {
        if (item == null || item.getType() != Material.IRON_NUGGET) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(NUGGET_KEY);
    }

    static boolean isIngot(ItemStack item) {
        if (item == null || item.getType() != Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(INGOT_KEY);
    }

    static List<ShapedRecipe> getRecipes(Plugin plugin) {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "meteoric_iron_ingot"), MeteoricIron.createMeteoricIronIngot())
            .shape("xxx", "xxx", "xxx")
            .setIngredient('x', MeteoricIron.createMeteoricIronNugget());
        recipe.setCategory(CraftingBookCategory.MISC);
        return List.of(recipe);
    }
}
