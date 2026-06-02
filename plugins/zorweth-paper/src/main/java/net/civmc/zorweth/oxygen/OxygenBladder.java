package net.civmc.zorweth.oxygen;

import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public final class OxygenBladder {

    private static final String SMALL_OXYGEN_BLADDER = "small_oxygen_bladder";
    private static final String OXYGEN_REBREATHER = "oxygen_rebreather";

    private OxygenBladder() {
    }

    public static void registerCustomItems() {
        createSmallOxygenBladder();
        createOxygenRebreather();
    }

    public static ItemStack createSmallOxygenBladder() {
        final ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("rabbit_hide"));
        item.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Small Oxygen Bladder", TextColor.color(140, 163, 177)));
        meta.lore(List.of(
            Component.text("A flexible reserve for thin Zorweth air.", NamedTextColor.WHITE),
            Component.text("Increases max oxygen to 4000", NamedTextColor.WHITE),
            Component.text("Automatically consumes oxygen items below 1000 oxygen", NamedTextColor.WHITE)
        ));
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem(SMALL_OXYGEN_BLADDER, item);
        return item;
    }

    public static ItemStack createOxygenRebreather() {
        final ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("conduit"));
        item.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Oxygen Rebreather", TextColor.color(140, 163, 177)));
        meta.lore(List.of(
            Component.text("An efficient apparatus for recycling oxygen.", NamedTextColor.WHITE),
            Component.text("Increases max oxygen to 30000", NamedTextColor.WHITE),
            Component.text("Automatically consumes oxygen items below 1000 oxygen", NamedTextColor.WHITE),
            Component.text("Reduced oxygen consumption while mining and regenerating", NamedTextColor.WHITE)
        ));
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem(OXYGEN_REBREATHER, item);
        return item;
    }

    private static final Map<String, Double> BLADDER_MAX = new HashMap<>();

    static {
        BLADDER_MAX.put(SMALL_OXYGEN_BLADDER, 4D);
        BLADDER_MAX.put(OXYGEN_REBREATHER, 30D);
    }

    public static boolean supportsActivity(ItemStack bladder, ActivityManager.Activity activity) {
        String key = CustomItem.getCustomItemKey(bladder);
        if (key != null) {
            if (OXYGEN_REBREATHER.equals(key)) {
                return !(activity == ActivityManager.Activity.COMBAT);
            } else if (SMALL_OXYGEN_BLADDER.equals(key)) {
                return !(activity == ActivityManager.Activity.COMBAT || activity == ActivityManager.Activity.MINING || activity == ActivityManager.Activity.REGENERATING);
            }
        }
        return activity == ActivityManager.Activity.IDLE || activity == ActivityManager.Activity.WALKING;
    }

    public static ItemStack getOxygenBladder(Player player) {
        ItemStack bladder = null;
        double max = 0;

        for (final ItemStack item : player.getInventory().getContents()) {
            String key = CustomItem.getCustomItemKey(item);
            if (BLADDER_MAX.containsKey(key) && BLADDER_MAX.get(key) > max) {
                max = BLADDER_MAX.get(key);
                bladder = item;
            }
        }

        return bladder;
    }

    public static double getMaxOxygen(Player player) {
        double max = OxygenManager.DEFAULT_MAX_OXYGEN;

        for (final ItemStack item : player.getInventory().getContents()) {
            String key = CustomItem.getCustomItemKey(item);
            if (BLADDER_MAX.containsKey(key) && BLADDER_MAX.get(key) > max) {
                max = BLADDER_MAX.get(key);
            }
        }

        return max;
    }

    public static CraftingRecipe getRecipe(final Plugin plugin) {
        final ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(plugin, SMALL_OXYGEN_BLADDER),
            createSmallOxygenBladder())
            .addIngredient(Material.STICK)
            .addIngredient(Material.STRING)
            .addIngredient(Material.FEATHER)
            .addIngredient(Material.IRON_NUGGET);
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        return recipe;
    }
}
