package net.civmc.heliodor.heliodor.recipe;

import java.util.List;
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

public interface HeliodorPickaxe {

    @SuppressWarnings("UnstableApiUsage")
    static ItemStack getItem() {
        ItemStack pickaxe = new ItemStack(Material.GOLDEN_PICKAXE);
        Damageable meta = (Damageable) pickaxe.getItemMeta();

        // TODO cancel repairs

        meta.displayName(Component.text("Heliodor Pickaxe", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.setRarity(ItemRarity.EPIC);
        meta.lore(List.of(
            Component.text("Break a block to determine", NamedTextColor.WHITE),
            Component.text("presence of nearby veins", NamedTextColor.WHITE)));
        meta.setMaxDamage(288); // expect 8 veins per pick
        ToolComponent tool = meta.getTool();
        tool.setDefaultMiningSpeed(0.25f);
        tool.setDamagePerBlock(1);
        tool.addRule(Tag.INCORRECT_FOR_GOLD_TOOL, 0.15f, true);
        tool.addRule(Tag.MINEABLE_PICKAXE, 0.5f, true);
        meta.setTool(tool);
        meta.setEnchantmentGlintOverride(true);
        meta.setFireResistant(true);
        pickaxe.setItemMeta(meta);
        return pickaxe;
    }

    static List<ShapedRecipe> getRecipes(Plugin plugin) {
        return List.of(categoryEquipment(new ShapedRecipe(new NamespacedKey(plugin, "heliodor_pickaxe"), HeliodorPickaxe.getItem())
            .shape("xxx", " s ", " s ")
            .setIngredient('x', HeliodorGem.createFinishedHeliodorGem())
            .setIngredient('s', Material.STICK)));
    }

    private static ShapedRecipe categoryEquipment(ShapedRecipe recipe) {
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        return recipe;
    }
}
