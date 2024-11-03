package net.civmc.heliodor.meteoriciron;

import java.util.List;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.civmc.heliodor.AnvilRepairListener;
import net.civmc.heliodor.HeliodorPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

@SuppressWarnings("UnstableApiUsage")
public interface MeteoricIronArmour {

    int CHESPLATE_DURABILITY = 9648;

    static ItemStack createHelmet() {
        ItemStack item = new ItemStack(Material.IRON_HELMET);
        Damageable meta = (Damageable) item.getItemMeta();

        meta.displayName(Component.text("Meteoric Iron Helmet", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.setRarity(ItemRarity.EPIC);
        meta.setMaxDamage(CHESPLATE_DURABILITY * 11 / 16);
        meta.addEnchant(Enchantment.UNBREAKING, 5, false);
        meta.addEnchant(Enchantment.RESPIRATION, 3, false);
        meta.addEnchant(Enchantment.AQUA_AFFINITY, 1, false);
        meta.addEnchant(Enchantment.PROTECTION, 4, false);
        meta.addEnchant(Enchantment.BLAST_PROTECTION, 4, false);
        meta.setFireResistant(true);
        setAttributes(meta, 3, 2, 0.03, "helmet", EquipmentSlotGroup.HEAD);
        AnvilRepairListener.setNoCombine(meta);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem("meteoric_iron_helmet", item);
        return item;
    }

    static ItemStack createChestplate() {
        ItemStack item = new ItemStack(Material.IRON_CHESTPLATE);
        Damageable meta = (Damageable) item.getItemMeta();

        meta.displayName(Component.text("Meteoric Iron Chestplate", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.setRarity(ItemRarity.EPIC);
        meta.setMaxDamage(CHESPLATE_DURABILITY);
        meta.addEnchant(Enchantment.UNBREAKING, 5, false);
        meta.addEnchant(Enchantment.PROTECTION, 4, false);
        meta.addEnchant(Enchantment.BLAST_PROTECTION, 4, false);
        meta.setFireResistant(true);
        setAttributes(meta, 8, 2, 0.03, "chestplate", EquipmentSlotGroup.CHEST);
        AnvilRepairListener.setNoCombine(meta);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem("meteoric_iron_chestplate", item);
        return item;
    }

    static ItemStack createLeggings() {
        ItemStack item = new ItemStack(Material.IRON_LEGGINGS);
        Damageable meta = (Damageable) item.getItemMeta();

        meta.displayName(Component.text("Meteoric Iron Leggings", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.setRarity(ItemRarity.EPIC);
        meta.setMaxDamage(CHESPLATE_DURABILITY * 15 / 16);
        meta.addEnchant(Enchantment.UNBREAKING, 5, false);
        meta.addEnchant(Enchantment.SWIFT_SNEAK, 3, false);
        meta.addEnchant(Enchantment.PROTECTION, 4, false);
        meta.addEnchant(Enchantment.BLAST_PROTECTION, 4, false);
        meta.setFireResistant(true);
        setAttributes(meta, 6, 2, 0.03, "leggings", EquipmentSlotGroup.LEGS);
        AnvilRepairListener.setNoCombine(meta);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem("meteoric_iron_leggings", item);
        return item;
    }

    static ItemStack createBoots() {
        ItemStack item = new ItemStack(Material.IRON_BOOTS);
        Damageable meta = (Damageable) item.getItemMeta();

        meta.displayName(Component.text("Meteoric Iron Boots", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.setRarity(ItemRarity.EPIC);
        meta.setMaxDamage(CHESPLATE_DURABILITY * 13 / 16);
        meta.addEnchant(Enchantment.UNBREAKING, 5, false);
        meta.addEnchant(Enchantment.FEATHER_FALLING, 4, false);
        meta.addEnchant(Enchantment.DEPTH_STRIDER, 3, false);
        meta.addEnchant(Enchantment.PROTECTION, 4, false);
        meta.addEnchant(Enchantment.BLAST_PROTECTION, 4, false);
        meta.setFireResistant(true);
        setAttributes(meta, 3, 2, 0.03, "boots", EquipmentSlotGroup.FEET);
        AnvilRepairListener.setNoCombine(meta);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem("meteoric_iron_boots", item);
        return item;
    }

    private static void setAttributes(ItemMeta meta, int armour, int toughness, double speed, String name, EquipmentSlotGroup group) {
        String prefix = "meteoric_iron_" + name;
        Multimap<Attribute, AttributeModifier> ams = HashMultimap.create();
        HeliodorPlugin plugin = JavaPlugin.getPlugin(HeliodorPlugin.class);
        ams.put(Attribute.GENERIC_ARMOR, new AttributeModifier(new NamespacedKey(plugin, prefix + "_armour"), armour, AttributeModifier.Operation.ADD_NUMBER, group));
        ams.put(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(new NamespacedKey(plugin, prefix + "_toughness"), toughness, AttributeModifier.Operation.ADD_NUMBER, group));
        ams.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(new NamespacedKey(plugin, prefix + "_speed"), speed, AttributeModifier.Operation.MULTIPLY_SCALAR_1, group));
        meta.setAttributeModifiers(ams);
    }

    static List<ShapedRecipe> getRecipes(Plugin plugin) {
        return List.of(
            categoryEquipment(new ShapedRecipe(new NamespacedKey(plugin, "meteoric_iron_helmet"), MeteoricIronArmour.createHelmet())
                .shape("xxx", "x x")
                .setIngredient('x', MeteoricIron.createIngot())),
            categoryEquipment(new ShapedRecipe(new NamespacedKey(plugin, "meteoric_iron_chestplate"), MeteoricIronArmour.createChestplate())
                .shape("x x", "xxx", "xxx")
                .setIngredient('x', MeteoricIron.createIngot())),
            categoryEquipment(new ShapedRecipe(new NamespacedKey(plugin, "meteoric_iron_leggings"), MeteoricIronArmour.createLeggings())
                .shape("xxx", "x x", "x x")
                .setIngredient('x', MeteoricIron.createIngot())),
            categoryEquipment(new ShapedRecipe(new NamespacedKey(plugin, "meteoric_iron_boots"), MeteoricIronArmour.createBoots())
                .shape("x x", "x x")
                .setIngredient('x', MeteoricIron.createIngot())));
    }

    private static ShapedRecipe categoryEquipment(ShapedRecipe recipe) {
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        return recipe;
    }
}
