package net.civmc.zorweth.oxygen;

import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public final class OxygenBladder {

    private static final String CUSTOM_ITEM_KEY = "small_oxygen_bladder";
    private static final NamespacedKey RESERVE_KEY = new NamespacedKey("zorweth", "oxygen_bladder_reserve");

    private OxygenBladder() {
    }

    public static ItemStack createSmallOxygenBladder() {
        final ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("rabbit_hide"));
        item.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Small Oxygen Bladder", TextColor.color(140, 163, 177)));
        meta.lore(List.of(
            Component.text("A flexible reserve for thin Zorweth air.", NamedTextColor.WHITE),
            Component.text("Increases max oxygen to 3000", NamedTextColor.WHITE),
            Component.text("Automatically consumes oxygen items below 1000 oxygen", NamedTextColor.WHITE)
        ));
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem(CUSTOM_ITEM_KEY, item);
        return item;
    }

    public static boolean isSmallOxygenBladder(final ItemStack item) {
        if (item == null || item.isEmpty()) {
            return false;
        }
        return CustomItem.isCustomItem(item, CUSTOM_ITEM_KEY);
    }

    public static double getReserve(final ItemStack item) {
        if (!isSmallOxygenBladder(item)) {
            return 0;
        }
        final ItemMeta meta = item.getItemMeta();
        return Math.max(0, meta.getPersistentDataContainer().getOrDefault(RESERVE_KEY, PersistentDataType.DOUBLE, 0D));
    }

    public static void setReserve(final ItemStack item, final double reserve) {
        if (!isSmallOxygenBladder(item)) {
            return;
        }
        final ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(RESERVE_KEY, PersistentDataType.DOUBLE, Math.max(0, reserve));
        item.setItemMeta(meta);
    }

    public static CraftingRecipe getRecipe(final Plugin plugin) {
        final ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(plugin, CUSTOM_ITEM_KEY),
            createSmallOxygenBladder())
            .addIngredient(Material.STICK)
            .addIngredient(Material.STRING)
            .addIngredient(Material.FEATHER)
            .addIngredient(Material.IRON_NUGGET);
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        return recipe;
    }
}
