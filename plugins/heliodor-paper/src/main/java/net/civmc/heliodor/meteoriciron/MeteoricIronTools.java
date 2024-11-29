package net.civmc.heliodor.meteoriciron;

import net.civmc.heliodor.AnvilRepairListener;
import net.civmc.heliodor.HeliodorPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public interface MeteoricIronTools {

    int DURABILITY = 48_600;

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
        if (silk) {
            CustomItem.registerCustomItem("meteoric_iron_pickaxe_silk", pickaxe);
        } else {
            CustomItem.registerCustomItem("meteoric_iron_pickaxe", pickaxe);
        }
        return pickaxe;
    }

    static ItemStack createAxe(boolean silk) {
        ItemStack axe = new ItemStack(Material.IRON_AXE);
        Damageable meta = (Damageable) axe.getItemMeta();

        meta.displayName(Component.text("Meteoric Iron Axe", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.setRarity(ItemRarity.EPIC);
        meta.lore(List.of(
            Component.text("Deals 2.5x reinforcement damage on wood products", NamedTextColor.WHITE),
            Component.text("Deals 2x reinforcement damage on iron and copper products", NamedTextColor.WHITE)));
        ToolComponent tool = meta.getTool();
        tool.setDamagePerBlock(1);
        tool.addRule(Material.CHEST, 30f, true);
        tool.addRule(Material.TRAPPED_CHEST, 30f, true);
        tool.addRule(Tag.FENCE_GATES, 30f, true);
        tool.addRule(Tag.WOODEN_PRESSURE_PLATES, 30f, true);
        tool.addRule(Tag.LOGS, 1000f, true);
        tool.addRule(Material.JUKEBOX, 30f, true);
        tool.addRule(Material.BARREL, 30f, true);
        tool.addRule(Tag.WOODEN_TRAPDOORS, 30f, true);
        tool.addRule(Tag.WOODEN_FENCES, 30f, true);
        tool.addRule(Tag.WOODEN_DOORS, 30f, true);
        tool.addRule(Tag.PLANKS, 30f, true);
        tool.addRule(Tag.WOODEN_SLABS, 30f, true);
        tool.addRule(Tag.WOODEN_STAIRS, 30f, true);
        tool.addRule(Material.CRAFTING_TABLE, 30f, true);
        tool.addRule(Material.IRON_DOOR, 50f, true);
        tool.addRule(Material.IRON_TRAPDOOR, 50f, true);
        tool.addRule(Material.IRON_BARS, 50f, true);
        tool.addRule(Material.DISPENSER, 50f, true);
        tool.addRule(Material.DROPPER, 50f, true);
        tool.addRule(Material.HOPPER, 50f, true);
        tool.addRule(Material.FURNACE, 50f, true);
        tool.addRule(Material.COPPER_TRAPDOOR, 50f, true);
        tool.addRule(Material.EXPOSED_COPPER_TRAPDOOR, 50f, true);
        tool.addRule(Material.OXIDIZED_COPPER_TRAPDOOR, 50f, true);
        tool.addRule(Material.WEATHERED_COPPER_TRAPDOOR, 50f, true);
        tool.addRule(Material.WAXED_COPPER_TRAPDOOR, 50f, true);
        tool.addRule(Material.WAXED_EXPOSED_COPPER_TRAPDOOR, 50f, true);
        tool.addRule(Material.WAXED_OXIDIZED_COPPER_TRAPDOOR, 50f, true);
        tool.addRule(Material.WAXED_WEATHERED_COPPER_TRAPDOOR, 50f, true);
        tool.addRule(Material.COPPER_DOOR, 50f, true);
        tool.addRule(Material.EXPOSED_COPPER_DOOR, 50f, true);
        tool.addRule(Material.OXIDIZED_COPPER_DOOR, 50f, true);
        tool.addRule(Material.WEATHERED_COPPER_DOOR, 50f, true);
        tool.addRule(Material.WAXED_COPPER_DOOR, 50f, true);
        tool.addRule(Material.WAXED_EXPOSED_COPPER_DOOR, 50f, true);
        tool.addRule(Material.WAXED_OXIDIZED_COPPER_DOOR, 50f, true);
        tool.addRule(Material.WAXED_WEATHERED_COPPER_DOOR, 50f, true);
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
        axe.setItemMeta(meta);
        if (silk) {
            CustomItem.registerCustomItem("meteoric_iron_axe_silk", axe);
        } else {
            CustomItem.registerCustomItem("meteoric_iron_axe", axe);
        }
        return axe;
    }

    static ItemStack createSword(int knocback) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        Damageable meta = (Damageable) sword.getItemMeta();

        meta.displayName(Component.text("Meteoric Iron Sword", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.setRarity(ItemRarity.EPIC);
        meta.lore(List.of(
            Component.text("Deals 1 second of Slowness I on hit", NamedTextColor.WHITE),
            Component.text("Instantly breaks cobwebs", NamedTextColor.WHITE)));
        ToolComponent tool = meta.getTool();
        tool.setDamagePerBlock(1);
        tool.addRule(Material.COBWEB, 200f, true);
        meta.setTool(tool);
        meta.setMaxDamage(DURABILITY);
        meta.addEnchant(Enchantment.FIRE_ASPECT, 2, false);
        meta.addEnchant(Enchantment.SHARPNESS, 5, false);
        if (knocback > 0) {
            meta.addEnchant(Enchantment.KNOCKBACK, knocback, false);
        }
        meta.setFireResistant(true);
        AnvilRepairListener.setNoCombine(meta);
        sword.setItemMeta(meta);
        if (knocback == 2) {
            CustomItem.registerCustomItem("meteoric_iron_sword_knockback", sword);
        } else if (knocback == 1) {
            CustomItem.registerCustomItem("meteoric_iron_sword_knockback1", sword);
        } else {
            CustomItem.registerCustomItem("meteoric_iron_sword", sword);
        }
        return sword;
    }

    static List<CraftingRecipe> getRecipes(Plugin plugin) {
        return List.of(
            categoryEquipment(new ShapedRecipe(new NamespacedKey(plugin, "meteoric_iron_pickaxe_silk_touch"), MeteoricIronTools.createPickaxe(true))
                .shape("xxx", "asa", " s ")
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
                .setIngredient('s', Material.STICK)),
            categoryEquipment(new ShapedRecipe(new NamespacedKey(plugin, "meteoric_iron_sword_knockback"), MeteoricIronTools.createSword(2))
                .shape("axa", " x ", " s ")
                .setIngredient('x', MeteoricIron.createIngot())
                .setIngredient('a', Material.AMETHYST_SHARD)
                .setIngredient('s', Material.STICK)),
            categoryEquipment(new ShapedRecipe(new NamespacedKey(plugin, "meteoric_iron_sword_knockback1"), MeteoricIronTools.createSword(1))
                .shape("ax ", " x ", " s ")
                .setIngredient('x', MeteoricIron.createIngot())
                .setIngredient('a', Material.AMETHYST_SHARD)
                .setIngredient('s', Material.STICK)),
            categoryEquipment(new ShapedRecipe(new NamespacedKey(plugin, "meteoric_iron_sword_sharpness"), MeteoricIronTools.createSword(0))
                .shape(" x ", " x ", " s ")
                .setIngredient('x', MeteoricIron.createIngot())
                .setIngredient('s', Material.STICK))
        );
    }

    private static ShapedRecipe categoryEquipment(ShapedRecipe recipe) {
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        return recipe;
    }
}
