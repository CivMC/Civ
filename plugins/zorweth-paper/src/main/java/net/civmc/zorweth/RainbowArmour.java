package net.civmc.zorweth;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public final class RainbowArmour {

    public static final String RAINBOW_HELMET = "rainbow_helmet";
    public static final String RAINBOW_CHESTPLATE = "rainbow_chestplate";
    public static final String RAINBOW_LEGGINGS = "rainbow_leggings";
    public static final String RAINBOW_BOOTS = "rainbow_boots";

    private static int ticks;

    private RainbowArmour() {
    }

    public static void registerCustomItems() {
        createArmour(RAINBOW_HELMET, Material.LEATHER_HELMET, "Rainbow Helmet");
        createArmour(RAINBOW_CHESTPLATE, Material.LEATHER_CHESTPLATE, "Rainbow Chestplate");
        createArmour(RAINBOW_LEGGINGS, Material.LEATHER_LEGGINGS, "Rainbow Leggings");
        createArmour(RAINBOW_BOOTS, Material.LEATHER_BOOTS, "Rainbow Boots");
    }

    public static void startTask(final ZorwethPlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, RainbowArmour::updateWornArmour, 0L, 2L);
    }

    private static ItemStack createArmour(final String key, final Material material, final String name) {
        final ItemStack item = new ItemStack(material);
        item.editMeta(LeatherArmorMeta.class, meta -> {
            meta.itemName(MiniMessage.miniMessage().deserialize("<rainbow>" + name));
            meta.setColor(Color.RED);
            meta.setEnchantmentGlintOverride(true);
        });
        CustomItem.registerCustomItem(key, item);
        return item;
    }

    private static void updateWornArmour() {
        ticks += 2;
        final Color color = getRainbowColor(ticks);
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final PlayerInventory inventory = player.getInventory();
            updateArmour(inventory.getHelmet(), color);
            updateArmour(inventory.getChestplate(), color);
            updateArmour(inventory.getLeggings(), color);
            updateArmour(inventory.getBoots(), color);
        }
    }

    private static void updateArmour(final ItemStack item, final Color color) {
        if (!isRainbowArmour(item)) {
            return;
        }
        item.editMeta(LeatherArmorMeta.class, meta -> meta.setColor(color));
    }

    private static boolean isRainbowArmour(final ItemStack item) {
        return CustomItem.isCustomItem(item, RAINBOW_HELMET)
            || CustomItem.isCustomItem(item, RAINBOW_CHESTPLATE)
            || CustomItem.isCustomItem(item, RAINBOW_LEGGINGS)
            || CustomItem.isCustomItem(item, RAINBOW_BOOTS);
    }

    private static Color getRainbowColor(final int ticks) {
        final float hue = (ticks % 240) / 240.0f;
        final java.awt.Color color = java.awt.Color.getHSBColor(hue, 0.85f, 1.0f);
        return Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
    }
}
