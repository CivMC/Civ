package net.civmc.zorweth.mechanics;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class TotemRecipes {
    public static List<CraftingRecipe> getRecipes() {
        ItemStack totemShard = new ItemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        totemShard.editMeta(meta -> {
            meta.customName(Component.text("Totem Shard", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        });

        ItemStack totemFragment = new ItemStack(Material.GOLD_NUGGET);
        totemFragment.editMeta(meta -> {
            meta.customName(Component.text("Totem Fragment", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        });

        return List.of(
            new ShapedRecipe(new NamespacedKey("zorweth", "totem_fragment"), totemShard)
                .shape("xxx", "xxx", "xxx")
                .setIngredient('x', totemFragment),
            new ShapedRecipe(new NamespacedKey("zorweth", "totem"), new ItemStack(Material.TOTEM_OF_UNDYING))
                .shape("xxx", "xxx", "xxx")
                .setIngredient('x', totemShard)
        );
    }
}
