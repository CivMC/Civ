package net.civmc.zorweth.oxygen;

import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public final class OxygenBladder {

    private static final String SMALL_OXYGEN_BLADDER = "small_oxygen_bladder";
    private static final String OXYGEN_REBREATHER = "oxygen_rebreather";
    private static final String OXYGEN_SUIT = "oxygen_suit";
    private static final NamespacedKey HELIODOR_NO_COMBINE = new NamespacedKey("heliodor", "no_combine");

    private OxygenBladder() {
    }

    public static void registerCustomItems() {
        createSmallOxygenBladder();
        createOxygenRebreather();
        createOxygenSuit();
    }

    public static ItemStack createSmallOxygenBladder() {
        final ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("rabbit_hide"));
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Small Oxygen Bladder", TextColor.color(140, 163, 177)));
        meta.lore(List.of(
            Component.text("A flexible reserve for thin Zorweth air.", NamedTextColor.WHITE),
            Component.text("Increases max oxygen to 4000", NamedTextColor.WHITE),
            Component.text("Automatically consumes oxygen items below 1000 oxygen", NamedTextColor.WHITE),
            Component.text("Place on head to use", NamedTextColor.WHITE)
        ));
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(HELIODOR_NO_COMBINE, PersistentDataType.BOOLEAN, true);
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem(SMALL_OXYGEN_BLADDER, item);
        return item;
    }

    public static ItemStack createOxygenRebreather() {
        final ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        item.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(Color.AQUA));
        item.addUnsafeEnchantment(Enchantment.PROTECTION, 2);
        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("conduit"));
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Oxygen Rebreather", TextColor.color(140, 163, 177)));
        meta.lore(List.of(
            Component.text("An efficient apparatus for recycling oxygen.", NamedTextColor.WHITE),
            Component.text("Increases max oxygen to 25000", NamedTextColor.WHITE),
            Component.text("Automatically consumes oxygen items below 1000 oxygen", NamedTextColor.WHITE),
            Component.text("Reduced oxygen consumption while mining and regenerating", NamedTextColor.WHITE),
            Component.text("Place on head to use", NamedTextColor.WHITE)
        ));
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(HELIODOR_NO_COMBINE, PersistentDataType.BOOLEAN, true);
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem(OXYGEN_REBREATHER, item);
        return item;
    }

    public static ItemStack createOxygenSuit() {
        final ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        item.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(Color.BLUE));
        item.addUnsafeEnchantment(Enchantment.PROTECTION, 5);
        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("end_crystal"));
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Oxygen Suit", TextColor.color(140, 163, 177)));
        meta.lore(List.of(
            Component.text("An efficient apparatus for recycling oxygen.", NamedTextColor.WHITE),
            Component.text("Increases max oxygen to 75000", NamedTextColor.WHITE),
            Component.text("Automatically consumes oxygen items below 1000 oxygen", NamedTextColor.WHITE),
            Component.text("Reduced oxygen consumption while in combat, mining, or regenerating", NamedTextColor.WHITE),
            Component.text("Place on head to use", NamedTextColor.WHITE)
        ));
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(HELIODOR_NO_COMBINE, PersistentDataType.BOOLEAN, true);
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem(OXYGEN_SUIT, item);
        return item;
    }

    private static final Map<String, Double> BLADDER_MAX = new HashMap<>();

    static {
        BLADDER_MAX.put(SMALL_OXYGEN_BLADDER, 4D);
        BLADDER_MAX.put(OXYGEN_REBREATHER, 25D);
        BLADDER_MAX.put(OXYGEN_SUIT, 75D);
    }

    public static boolean supportsActivity(ItemStack bladder, ActivityManager.Activity activity) {
        String key = CustomItem.getCustomItemKey(bladder);
        if (key != null) {
            if (OXYGEN_REBREATHER.equals(key)) {
                return !(activity == ActivityManager.Activity.COMBAT);
            } else if (SMALL_OXYGEN_BLADDER.equals(key)) {
                return !(activity == ActivityManager.Activity.COMBAT || activity == ActivityManager.Activity.MINING || activity == ActivityManager.Activity.REGENERATING);
            } else if (OXYGEN_SUIT.equals(key)) {
                return true;
            }
        }
        return activity == ActivityManager.Activity.IDLE || activity == ActivityManager.Activity.WALKING;
    }

    public static ItemStack getOxygenBladder(Player player) {
        final ItemStack item = player.getInventory().getHelmet();
        final String key = CustomItem.getCustomItemKey(item);
        if (BLADDER_MAX.containsKey(key)) {
            return item;
        }
        return null;
    }

    public static double getMaxOxygen(Player player) {
        final ItemStack item = player.getInventory().getHelmet();
        final String key = CustomItem.getCustomItemKey(item);
        return BLADDER_MAX.getOrDefault(key, OxygenManager.DEFAULT_MAX_OXYGEN);
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
