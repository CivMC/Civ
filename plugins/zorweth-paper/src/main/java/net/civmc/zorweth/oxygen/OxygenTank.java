package net.civmc.zorweth.oxygen;

import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public final class OxygenTank {

    public static final String EMPTY_BASIC_OXYGEN_TANK = "empty_basic_oxygen_tank";
    public static final String FILLED_BASIC_OXYGEN_TANK = "filled_basic_oxygen_tank";
    public static final double BASIC_OXYGEN_TANK_AMOUNT = 8D;
    public static final double ADVANCED_OXYGEN_TANK_AMOUNT = 34D;
    private static final String EMPTY_ADVANCED_OXYGEN_TANK = "empty_advanced_oxygen_tank";
    private static final String FILLED_ADVANCED_OXYGEN_TANK = "filled_advanced_oxygen_tank";

    private OxygenTank() {
    }

    public static void registerCustomItems() {
        createEmptyBasicOxygenTank();
        createFilledBasicOxygenTank();
        createEmptyAdvancedOxygenTank();
        createFilledAdvancedOxygenTank();
        createOxygenFilters();
    }

    public static ItemStack createEmptyBasicOxygenTank() {
        final ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("mace"));
        item.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Empty Basic Oxygen Tank", TextColor.color(140, 163, 177)));
        meta.lore(List.of(
            Component.text("A simple aluminium tank for storing oxygen.", NamedTextColor.WHITE),
            Component.text("Refill in an oxygen-rich biome.", NamedTextColor.WHITE)
        ));
        meta.setEnchantmentGlintOverride(false);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem(EMPTY_BASIC_OXYGEN_TANK, item);
        return item;
    }

    public static ItemStack createFilledBasicOxygenTank() {
        final ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("mace"));
        item.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Filled Basic Oxygen Tank", TextColor.color(140, 163, 177)));
        meta.lore(List.of(
            Component.text("A simple aluminium tank filled with oxygen.", NamedTextColor.WHITE),
            Component.text("Provides 8000 oxygen", NamedTextColor.WHITE)
        ));
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem(FILLED_BASIC_OXYGEN_TANK, item);
        return item;
    }

    public static ItemStack createEmptyAdvancedOxygenTank() {
        final ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("mace"));
        item.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Empty Advanced Oxygen Tank", TextColor.color(90, 136, 140)));
        meta.lore(List.of(
            Component.text("A reinforced aluminium tank for storing compressed oxygen.", NamedTextColor.WHITE),
            Component.text("Refill in an oxygen-rich biome.", NamedTextColor.WHITE)
        ));
        meta.setEnchantmentGlintOverride(false);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem(EMPTY_ADVANCED_OXYGEN_TANK, item);
        return item;
    }

    public static ItemStack createFilledAdvancedOxygenTank() {
        final ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("mace"));
        item.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Filled Advanced Oxygen Tank", TextColor.color(90, 136, 140)));
        meta.lore(List.of(
            Component.text("A sturdy aluminium tank filled with oxygen.", NamedTextColor.WHITE),
            Component.text("Provides 34000 oxygen", NamedTextColor.WHITE)
        ));
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem(FILLED_ADVANCED_OXYGEN_TANK, item);
        return item;
    }

    public static ItemStack createOxygenFilters() {
        final ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("phantom_membrane"));
        item.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Oxygen Filters", TextColor.color(90, 136, 140)));
        meta.lore(List.of(
            Component.text("Ingredient for refilling advanced oxygen tanks.", NamedTextColor.WHITE)
        ));
        meta.setEnchantmentGlintOverride(false);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem("oxygen_filters", item);
        return item;
    }

    public static boolean isFilledBasicOxygenTank(final ItemStack item) {
        return CustomItem.isCustomItem(item, FILLED_BASIC_OXYGEN_TANK);
    }

    public static boolean isFilledAdvancedOxygenTank(final ItemStack item) {
        return CustomItem.isCustomItem(item, FILLED_ADVANCED_OXYGEN_TANK);
    }
}
