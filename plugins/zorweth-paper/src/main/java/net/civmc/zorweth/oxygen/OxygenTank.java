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
    public static final double BASIC_OXYGEN_TANK_AMOUNT = 7D;

    private OxygenTank() {
    }

    public static void registerCustomItems() {
        createEmptyBasicOxygenTank();
        createFilledBasicOxygenTank();
    }

    public static ItemStack createEmptyBasicOxygenTank() {
        final ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("mace"));
        item.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Empty Basic Oxygen Tank", TextColor.color(140, 163, 177)));
        meta.lore(List.of(
            Component.text("A sturdy aluminium tank for storing oxygen.", NamedTextColor.WHITE),
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
            Component.text("A sturdy aluminium tank filled with oxygen.", NamedTextColor.WHITE),
            Component.text("Provides 7000 oxygen", NamedTextColor.WHITE)
        ));
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem(FILLED_BASIC_OXYGEN_TANK, item);
        return item;
    }

    public static boolean isFilledBasicOxygenTank(final ItemStack item) {
        return CustomItem.isCustomItem(item, FILLED_BASIC_OXYGEN_TANK);
    }
}
