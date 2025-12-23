package net.civmc.heliodor.farmbeacon;

import java.util.List;
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

public interface FarmBeacon {
    CustomItemFactory FARM_BEACON = CustomItem.registerCustomItem("farm_beacon", () -> {
        final ItemStack item = ItemStack.of(Material.BEACON);
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Farm Beacon", NamedTextColor.LIGHT_PURPLE));
        meta.lore(List.of(
            Component.text("Allows crops to grow larger and more bountiful", NamedTextColor.WHITE),
            Component.text("Increases soil fertility +6%", NamedTextColor.WHITE),
            Component.text("within a 20 block radius.", NamedTextColor.WHITE),
            Component.text("Takes 6 days to become effective.", NamedTextColor.WHITE),
            Component.text("Requires a beacon base and sky exposure", NamedTextColor.WHITE)
        ));
        meta.setEnchantmentGlintOverride(true);
        meta.setFireResistant(true);
        item.setItemMeta(meta);
        return item;
    });

    static boolean isFarmBeacon(ItemStack item) {
        return CustomItem.isCustomItem(item, "farm_beacon");
    }

    static List<CraftingRecipe> getRecipes(Plugin plugin) {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "meteoric_farm_beacon"), FARM_BEACON.createItem())
            .shape("nnn", "nbn", "nnn")
            .setIngredient('n', Material.NETHERITE_INGOT)
            .setIngredient('b', Material.BEACON);
        recipe.setCategory(CraftingBookCategory.MISC);
        return List.of(recipe);
    }
}
