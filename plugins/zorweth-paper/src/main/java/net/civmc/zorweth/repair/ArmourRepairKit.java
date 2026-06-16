package net.civmc.zorweth.repair;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;
import java.util.List;

public class ArmourRepairKit {
    public static final String ARMOUR_REPAIR_KIT = "armour_repair_kit";

    public static ItemStack createArmourRepairKit() {
        final ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("netherite_nautilus_armor"));
        item.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Armour Repair Kit", TextColor.color(153, 39, 84)));
        meta.lore(List.of(
            Component.text("Repairs +20% / +15% / +10% / +5%", NamedTextColor.WHITE),
            Component.text("Less effective each time.", NamedTextColor.WHITE),
            Component.text("Craft with any piece of armour. One time use.", NamedTextColor.WHITE)
        ));
        item.setItemMeta(meta);
        CustomItem.registerCustomItem(ARMOUR_REPAIR_KIT, item);
        return item;
    }
}
