package net.civmc.heliodor.heliodor.recipe;

import net.civmc.heliodor.heliodor.HeliodorGem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.plugin.Plugin;
import java.util.List;

public interface MeteoritePickaxe {

    @SuppressWarnings("UnstableApiUsage")
    static ItemStack getItem(boolean silk) {
        ItemStack pickaxe = new ItemStack(Material.IRON_PICKAXE);
        Damageable meta = (Damageable) pickaxe.getItemMeta();

        // TODO cancel repairs

        meta.displayName(Component.text("Meteorite Pickaxe", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.setRarity(ItemRarity.EPIC);
        meta.lore(List.of(
            Component.text("Instantly breaks deepslate and stone,", NamedTextColor.WHITE),
            Component.text("and otherwise equivalent to diamond", NamedTextColor.WHITE)));
        ToolComponent tool = meta.getTool();
        tool.setDamagePerBlock(1);
        tool.addRule(Tag.BASE_STONE_OVERWORLD, 1000f, true);
        tool.addRule(Tag.MINEABLE_PICKAXE, 8f, true);
        meta.setTool(tool);
        meta.setMaxDamage(60_000);
        meta.addEnchant(Enchantment.EFFICIENCY, 5, false);
        if (silk) {
            meta.addEnchant(Enchantment.SILK_TOUCH, 1, false);
        } else {
            meta.addEnchant(Enchantment.FORTUNE, 3, false);
        }
        meta.setFireResistant(true);
        pickaxe.setItemMeta(meta);
        return pickaxe;
    }

    static List<ShapedRecipe> getRecipes(Plugin plugin) {
        return List.of(categoryEquipment(new ShapedRecipe(new NamespacedKey(plugin, "meteorite_pickaxe_silk_touch"), MeteoritePickaxe.getItem(true))
                .shape("xxx", "asa", " s ")
                .setIngredient('x', Material.IRON_BLOCK)
                .setIngredient('a', Material.AMETHYST_SHARD)
                .setIngredient('s', Material.STICK)),
            categoryEquipment(new ShapedRecipe(new NamespacedKey(plugin, "meteorite_pickaxe_fortune"), MeteoritePickaxe.getItem(false))
                .shape("xxx", " s ", " s ")
                .setIngredient('x', Material.IRON_BLOCK)
                .setIngredient('s', Material.STICK)));
    }

    private static ShapedRecipe categoryEquipment(ShapedRecipe recipe) {
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        return recipe;
    }
}
