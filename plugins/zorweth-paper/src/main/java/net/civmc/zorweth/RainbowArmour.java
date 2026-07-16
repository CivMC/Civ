package net.civmc.zorweth;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitTask;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public final class RainbowArmour {

    public static final String RAINBOW_HELMET = "rainbow_helmet";
    public static final String RAINBOW_CHESTPLATE = "rainbow_chestplate";
    public static final String RAINBOW_LEGGINGS = "rainbow_leggings";
    public static final String RAINBOW_BOOTS = "rainbow_boots";

    private static long ticks;
    private static BukkitTask task;

private RainbowArmour() {
    }

    public static void registerCustomItems() {
        createArmour(RAINBOW_HELMET, Material.LEATHER_HELMET, "Rainbow Helmet");
        createArmour(RAINBOW_CHESTPLATE, Material.LEATHER_CHESTPLATE, "Rainbow Chestplate");
        createArmour(RAINBOW_LEGGINGS, Material.LEATHER_LEGGINGS, "Rainbow Leggings");
        createArmour(RAINBOW_BOOTS, Material.LEATHER_BOOTS, "Rainbow Boots");
    }

    public static void startTask(final ZorwethPlugin plugin) {
        if (task != null) {
            task.cancel();
        }
        ticks = 0;
        task = Bukkit.getScheduler().runTaskTimer(plugin, RainbowArmour::updateWornArmour, 0L, 2L);
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
        for (final Player wearer : Bukkit.getOnlinePlayers()) {
            final Map<EquipmentSlot, ItemStack> equipment = getRainbowArmour(wearer.getInventory(), color);
            if (!equipment.isEmpty()) {
                sendEquipment(wearer, equipment);
            }
        }
    }

    private static Map<EquipmentSlot, ItemStack> getRainbowArmour(final PlayerInventory inventory,
                                                                   final Color color) {
        final Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
        addRainbowArmour(equipment, EquipmentSlot.HEAD, inventory.getHelmet(), color);
        addRainbowArmour(equipment, EquipmentSlot.CHEST, inventory.getChestplate(), color);
        addRainbowArmour(equipment, EquipmentSlot.LEGS, inventory.getLeggings(), color);
        addRainbowArmour(equipment, EquipmentSlot.FEET, inventory.getBoots(), color);
        return equipment;
    }

    private static void addRainbowArmour(final Map<EquipmentSlot, ItemStack> equipment, final EquipmentSlot slot,
                                         final ItemStack item, final Color color) {
        if (!isRainbowArmour(item)) {
            return;
        }
        final ItemStack displayedItem = item.clone();
        if (displayedItem.editMeta(LeatherArmorMeta.class, meta -> meta.setColor(color))) {
            equipment.put(slot, displayedItem);
        }
    }

    private static Map<EquipmentSlot, ItemStack> getActualArmour(final PlayerInventory inventory) {
        final Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
        equipment.put(EquipmentSlot.HEAD, inventory.getHelmet());
        equipment.put(EquipmentSlot.CHEST, inventory.getChestplate());
        equipment.put(EquipmentSlot.LEGS, inventory.getLeggings());
        equipment.put(EquipmentSlot.FEET, inventory.getBoots());
        return equipment;
    }

    private static void sendEquipment(final Player wearer, final Map<EquipmentSlot, ItemStack> equipment) {
        final Set<Player> viewers = new HashSet<>(wearer.getTrackedBy());
        viewers.add(wearer);
        for (final Player viewer : viewers) {
            if (viewer.canSee(wearer)) {
                viewer.sendEquipmentChange(wearer, equipment);
            }
        }
    }

    private static boolean isRainbowArmour(final ItemStack item) {
        return CustomItem.isCustomItem(item, RAINBOW_HELMET)
            || CustomItem.isCustomItem(item, RAINBOW_CHESTPLATE)
            || CustomItem.isCustomItem(item, RAINBOW_LEGGINGS)
            || CustomItem.isCustomItem(item, RAINBOW_BOOTS);
    }

    private static Color getRainbowColor(final long ticks) {
        final float hue = (ticks % 240L) / 240.0f;
        final java.awt.Color color = java.awt.Color.getHSBColor(hue, 0.85f, 1.0f);
        return Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
    }
}
