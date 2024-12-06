package net.civmc.heliodor.heliodor;

import java.util.List;
import net.civmc.heliodor.AnvilRepairListener;
import net.civmc.heliodor.HeliodorPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public interface HeliodorPickaxe {

    NamespacedKey PICKAXE_KEY = new NamespacedKey(JavaPlugin.getPlugin(HeliodorPlugin.class), "heliodor_pickaxe");

    @SuppressWarnings("UnstableApiUsage")
    static ItemStack getItem() {
        ItemStack pickaxe = new ItemStack(Material.GOLDEN_PICKAXE);
        Damageable meta = (Damageable) pickaxe.getItemMeta();

        meta.displayName(Component.text("Heliodor Pickaxe", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.setRarity(ItemRarity.EPIC);
        meta.lore(List.of(
            Component.text("Break a block to determine", NamedTextColor.WHITE),
            Component.text("presence of nearby veins", NamedTextColor.WHITE),
            Component.empty(),
            Component.text("Shift right click while sneaking to count", NamedTextColor.WHITE),
            Component.text("global veins (costs " + VeinDetectListener.DURABILITY_COST + " durability)", NamedTextColor.WHITE)));
        meta.setMaxDamage(288); // expect 8 veins per pick
        ToolComponent tool = meta.getTool();
        tool.setDefaultMiningSpeed(0.25f);
        tool.setDamagePerBlock(1);
        tool.addRule(Tag.INCORRECT_FOR_GOLD_TOOL, 0.15f, true);
        tool.addRule(Tag.MINEABLE_PICKAXE, 0.5f, true);
        meta.setTool(tool);
        meta.setEnchantmentGlintOverride(true);
        meta.setFireResistant(true);
        AnvilRepairListener.setNoCombine(meta);
        meta.getPersistentDataContainer().set(PICKAXE_KEY, PersistentDataType.BOOLEAN, true);
        pickaxe.setItemMeta(meta);
        return pickaxe;
    }

    static boolean isPickaxe(ItemStack item) {
        if (item == null || item.getType() != Material.GOLDEN_PICKAXE) {
            return false;
        }
        return item.getPersistentDataContainer().has(PICKAXE_KEY);
    }

    static List<CraftingRecipe> getRecipes(Plugin plugin) {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "heliodor_pickaxe"), HeliodorPickaxe.getItem())
            .shape("xxx", " s ", " s ")
            .setIngredient('x', HeliodorGem.createFinishedHeliodorGem())
            .setIngredient('s', Material.STICK);
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        return List.of(recipe);
    }
}
