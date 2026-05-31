package net.civmc.zorweth.mechanics;

import java.util.List;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public class Fuel {

    public static ItemStack createCrudeOil() {
        ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        item.setData(DataComponentTypes.ITEM_MODEL, new NamespacedKey("minecraft", "charcoal"));
        ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Crude Oil", TextColor.color(64, 52, 40)));
        meta.lore(List.of(Component.text("It smells pretty noxious.", NamedTextColor.WHITE),
            Component.text("Can be refined to rocket fuel.", NamedTextColor.WHITE)));
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem("crude_oil", item);

        return item;
    }

    public static ItemStack createRocketFuel() {
        ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        item.setData(DataComponentTypes.ITEM_MODEL, new NamespacedKey("minecraft", "resin_brick"));
        ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Rocket Fuel", TextColor.color(162, 113, 52)));
        meta.lore(List.of(Component.text("Used to refuel rockets.", NamedTextColor.WHITE),
            Component.text("Each item represents 4 kg of fuel.", NamedTextColor.WHITE)));
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem("rocket_fuel", item);

        return item;
    }

    public static boolean isRocketFuel(final ItemStack item) {
        return CustomItem.isCustomItem(item, "rocket_fuel");
    }
}
