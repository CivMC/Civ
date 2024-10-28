package net.civmc.heliodor.meteoriciron;

import net.civmc.heliodor.AnvilRepairListener;
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

@SuppressWarnings("UnstableApiUsage")
public interface MeteoricIronTools {

    int DURABILITY = 54_000;

    static ItemStack createPickaxe(boolean silk) {
        ItemStack pickaxe = new ItemStack(Material.IRON_PICKAXE);
        Damageable meta = (Damageable) pickaxe.getItemMeta();

        meta.displayName(Component.text("Meteoric Iron Pickaxe", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.setRarity(ItemRarity.EPIC);
        meta.lore(List.of(
            Component.text("Instantly breaks deepslate and stone,", NamedTextColor.WHITE),
            Component.text("and otherwise equivalent to diamond", NamedTextColor.WHITE)));
        ToolComponent tool = meta.getTool();
        tool.setDamagePerBlock(1);
        tool.addRule(Tag.BASE_STONE_OVERWORLD, 1000f, true);
        tool.addRule(Tag.MINEABLE_PICKAXE, 8f, true);
        meta.setTool(tool);
        meta.setMaxDamage(DURABILITY);
        meta.addEnchant(Enchantment.EFFICIENCY, 5, false);
        if (silk) {
            meta.addEnchant(Enchantment.SILK_TOUCH, 1, false);
        } else {
            meta.addEnchant(Enchantment.FORTUNE, 3, false);
        }
        meta.setFireResistant(true);
        AnvilRepairListener.setNoCombine(meta);
        pickaxe.setItemMeta(meta);
        return pickaxe;
    }

    static ItemStack createAxe(boolean silk) {
        ItemStack pickaxe = new ItemStack(Material.IRON_AXE);
        Damageable meta = (Damageable) pickaxe.getItemMeta();

        meta.displayName(Component.text("Meteoric Iron Axe", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.setRarity(ItemRarity.EPIC);
        meta.lore(List.of(
            Component.text("Instantly breaks many wood products,", NamedTextColor.WHITE),
            Component.text("and can break some iron ones too", NamedTextColor.WHITE)));
        ToolComponent tool = meta.getTool();
        tool.setDamagePerBlock(1);
        tool.addRule(Material.CHEST, 1000f, true);
        tool.addRule(Material.TRAPPED_CHEST, 1000f, true);
        tool.addRule(Tag.FENCE_GATES, 1000f, true);
        tool.addRule(Tag.WOODEN_PRESSURE_PLATES, 1000f, true);
        tool.addRule(Tag.LOGS, 1000f, true);
        tool.addRule(Material.JUKEBOX, 1000f, true);
        tool.addRule(Material.BARREL, 1000f, true);
        tool.addRule(Tag.WOODEN_TRAPDOORS, 1000f, true);
        tool.addRule(Tag.WOODEN_FENCES, 1000f, true);
        tool.addRule(Tag.WOODEN_DOORS, 1000f, true);
        // 2 ticks for iron stuff
        tool.addRule(Material.IRON_DOOR, 50f, true);
        tool.addRule(Material.IRON_TRAPDOOR, 50f, true);
        tool.addRule(Material.IRON_BARS, 50f, true);
        tool.addRule(Tag.MINEABLE_AXE, 8f, true);
        meta.setTool(tool);
        meta.setMaxDamage(DURABILITY);
        meta.addEnchant(Enchantment.EFFICIENCY, 5, false);
        if (silk) {
            meta.addEnchant(Enchantment.SILK_TOUCH, 1, false);
        } else {
            meta.addEnchant(Enchantment.FORTUNE, 3, false);
        }
        meta.setFireResistant(true);
        AnvilRepairListener.setNoCombine(meta);
        pickaxe.setItemMeta(meta);
        return pickaxe;
    }

    static List<ShapedRecipe> getRecipes(Plugin plugin) {
        return List.of(
            categoryEquipment(new ShapedRecipe(new NamespacedKey(plugin, "meteoric_iron_pickaxe_silk_touch"), MeteoricIronTools.createPickaxe(true))
                .shape("xxx", "asa", "asa")
                .setIngredient('x', MeteoricIron.createIngot())
                .setIngredient('a', Material.AMETHYST_SHARD)
                .setIngredient('s', Material.STICK)),
            categoryEquipment(new ShapedRecipe(new NamespacedKey(plugin, "meteoric_iron_pickaxe_fortune"), MeteoricIronTools.createPickaxe(false))
                .shape("xxx", " s ", " s ")
                .setIngredient('x', MeteoricIron.createIngot())
                .setIngredient('s', Material.STICK)),
            categoryEquipment(new ShapedRecipe(new NamespacedKey(plugin, "meteoric_iron_axe_silk_touch"), MeteoricIronTools.createAxe(true))
                .shape("xxa", "xs ", "as ")
                .setIngredient('x', MeteoricIron.createIngot())
                .setIngredient('a', Material.AMETHYST_SHARD)
                .setIngredient('s', Material.STICK)),
            categoryEquipment(new ShapedRecipe(new NamespacedKey(plugin, "meteoric_iron_axe_fortune"), MeteoricIronTools.createAxe(false))
                .shape("xx ", "xs ", " s ")
                .setIngredient('x', MeteoricIron.createIngot())
                .setIngredient('s', Material.STICK))
        );
    }

    private static ShapedRecipe categoryEquipment(ShapedRecipe recipe) {
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        return recipe;
    }
}
