package net.civmc.kitpvp.gui.selection;

import net.civmc.kitpvp.data.Kit;
import net.civmc.kitpvp.data.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItem;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;

public class GenericArmourSelectionGui extends ItemSelectionGui {

    public GenericArmourSelectionGui(KitPvpDao dao, Player player, int slot, Kit kit, Runnable parent, EditKitGui gui) {
        super(dao, "Armour", player, slot, kit, parent, gui);
    }

    @Override
    public void addItems(ClickableInventory inventory) {
        inventory.setSlot(toClickable(new ItemStack(Material.LEATHER_HELMET)), 0);
        inventory.setSlot(toClickable(new ItemStack(Material.LEATHER_CHESTPLATE)), 9);
        inventory.setSlot(toClickable(new ItemStack(Material.LEATHER_LEGGINGS)), 18);
        inventory.setSlot(toClickable(new ItemStack(Material.LEATHER_BOOTS)), 27);

        inventory.setSlot(toClickable(new ItemStack(Material.GOLDEN_HELMET)), 1);
        inventory.setSlot(toClickable(new ItemStack(Material.GOLDEN_CHESTPLATE)), 10);
        inventory.setSlot(toClickable(new ItemStack(Material.GOLDEN_LEGGINGS)), 19);
        inventory.setSlot(toClickable(new ItemStack(Material.GOLDEN_BOOTS)), 28);

        inventory.setSlot(toClickable(new ItemStack(Material.CHAINMAIL_HELMET)), 2);
        inventory.setSlot(toClickable(new ItemStack(Material.CHAINMAIL_CHESTPLATE)), 11);
        inventory.setSlot(toClickable(new ItemStack(Material.CHAINMAIL_LEGGINGS)), 20);
        inventory.setSlot(toClickable(new ItemStack(Material.CHAINMAIL_BOOTS)), 29);

        inventory.setSlot(toClickable(new ItemStack(Material.IRON_HELMET)), 3);
        inventory.setSlot(toClickable(new ItemStack(Material.IRON_CHESTPLATE)), 12);
        inventory.setSlot(toClickable(new ItemStack(Material.IRON_LEGGINGS)), 21);
        inventory.setSlot(toClickable(new ItemStack(Material.IRON_BOOTS)), 30);

        inventory.setSlot(toClickable(new ItemStack(Material.DIAMOND_HELMET)), 4);
        inventory.setSlot(toClickable(new ItemStack(Material.DIAMOND_CHESTPLATE)), 13);
        inventory.setSlot(toClickable(new ItemStack(Material.DIAMOND_LEGGINGS)), 22);
        inventory.setSlot(toClickable(new ItemStack(Material.DIAMOND_BOOTS)), 31);

        inventory.setSlot(toClickable(new ItemStack(Material.NETHERITE_HELMET)), 5);
        inventory.setSlot(toClickable(new ItemStack(Material.NETHERITE_CHESTPLATE)), 14);
        inventory.setSlot(toClickable(new ItemStack(Material.NETHERITE_LEGGINGS)), 23);
        inventory.setSlot(toClickable(new ItemStack(Material.NETHERITE_BOOTS)), 32);


        inventory.setSlot(toClickable(CustomItem.getCustomItem("meteoric_iron_helmet")), 6);
        inventory.setSlot(toClickable(CustomItem.getCustomItem("meteoric_iron_chestplate")), 15);
        inventory.setSlot(toClickable(CustomItem.getCustomItem("meteoric_iron_leggings")), 24);
        inventory.setSlot(toClickable(CustomItem.getCustomItem("meteoric_iron_boots")), 33);

        inventory.setSlot(toClickable(new ItemStack(Material.TURTLE_HELMET)), 7);
        inventory.setSlot(toClickable(new ItemStack(Material.ELYTRA)), 16);
    }
}
