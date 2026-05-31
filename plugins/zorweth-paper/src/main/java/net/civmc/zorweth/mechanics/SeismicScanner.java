package net.civmc.zorweth.mechanics;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.UseCooldown;
import java.util.List;
import net.kyori.adventure.key.Key;
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
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public class SeismicScanner {

    public static final Key COOLDOWN_GROUP = new NamespacedKey("zorweth", "seismic_scanner_cooldown");

    public static ItemStack createSeismicScanner() {
        ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        item.setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(5).cooldownGroup(COOLDOWN_GROUP).build());
        ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Seismic Scanner", NamedTextColor.DARK_AQUA));
        meta.lore(List.of(Component.text("An essential tool for prospecting crude oil deposits", NamedTextColor.WHITE),
            Component.text("Can detect deposits up to 200 blocks away", NamedTextColor.WHITE),
            Component.text("Right click to use", NamedTextColor.WHITE)));
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem("seismic_scanner", item);

        return item;
    }

    static boolean isSeismicScanner(ItemStack item) {
        return CustomItem.isCustomItem(item, "seismic_scanner");
    }

    static CraftingRecipe getRecipe(Plugin plugin) {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "seismic_scanner"), SeismicScanner.createSeismicScanner())
            .shape(" x ", "xox", " x ")
            .setIngredient('x', Material.GOLD_INGOT)
            .setIngredient('o', Material.AMETHYST_SHARD);
        recipe.setCategory(CraftingBookCategory.MISC);
        return recipe;
    }
}
