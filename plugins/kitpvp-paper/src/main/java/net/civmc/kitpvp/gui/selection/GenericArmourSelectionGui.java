package net.civmc.kitpvp.gui.selection;

import net.civmc.kitpvp.kit.Kit;
import net.civmc.kitpvp.kit.KitCustomItem;
import net.civmc.kitpvp.kit.KitItem;
import net.civmc.kitpvp.kit.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import net.civmc.kitpvp.kit.KitCost;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;

public class GenericArmourSelectionGui extends ItemSelectionGui {

    public GenericArmourSelectionGui(KitPvpDao dao, Player player, int slot, Kit kit, Runnable parent, EditKitGui gui) {
        super(dao, "Armour", player, slot, kit, parent, gui);
    }

    private Clickable toClickable(KitItem kitItem) {
        ItemStack item = new ItemStack(kitItem.getItem());
        return toClickable(KitCost.setPoints(item, kitItem.getCost()), item);
    }

    private Clickable toClickable(KitCustomItem kitItem) {
        ItemStack item = CustomItem.getCustomItem(kitItem.getItem());
        return toClickable(KitCost.setPoints(item, kitItem.getCost()), item);
    }

    @Override
    public void addItems(ClickableInventory inventory) {
        inventory.setSlot(toClickable(KitItem.LEATHER_HELMET), 0);
        inventory.setSlot(toClickable(KitItem.LEATHER_CHESTPLATE), 9);
        inventory.setSlot(toClickable(KitItem.LEATHER_LEGGINGS), 18);
        inventory.setSlot(toClickable(KitItem.LEATHER_BOOTS), 27);

        inventory.setSlot(toClickable(KitItem.GOLDEN_HELMET), 1);
        inventory.setSlot(toClickable(KitItem.GOLDEN_CHESTPLATE), 10);
        inventory.setSlot(toClickable(KitItem.GOLDEN_LEGGINGS), 19);
        inventory.setSlot(toClickable(KitItem.GOLDEN_BOOTS), 28);

        inventory.setSlot(toClickable(KitItem.CHAINMAIL_HELMET), 2);
        inventory.setSlot(toClickable(KitItem.CHAINMAIL_CHESTPLATE), 11);
        inventory.setSlot(toClickable(KitItem.CHAINMAIL_LEGGINGS), 20);
        inventory.setSlot(toClickable(KitItem.CHAINMAIL_BOOTS), 29);

        inventory.setSlot(toClickable(KitItem.IRON_HELMET), 3);
        inventory.setSlot(toClickable(KitItem.IRON_CHESTPLATE), 12);
        inventory.setSlot(toClickable(KitItem.IRON_LEGGINGS), 21);
        inventory.setSlot(toClickable(KitItem.IRON_BOOTS), 30);

        inventory.setSlot(toClickable(KitItem.DIAMOND_HELMET), 4);
        inventory.setSlot(toClickable(KitItem.DIAMOND_CHESTPLATE), 13);
        inventory.setSlot(toClickable(KitItem.DIAMOND_LEGGINGS), 22);
        inventory.setSlot(toClickable(KitItem.DIAMOND_BOOTS), 31);

        inventory.setSlot(toClickable(KitItem.NETHERITE_HELMET), 5);
        inventory.setSlot(toClickable(KitItem.NETHERITE_CHESTPLATE), 14);
        inventory.setSlot(toClickable(KitItem.NETHERITE_LEGGINGS), 23);
        inventory.setSlot(toClickable(KitItem.NETHERITE_BOOTS), 32);

        inventory.setSlot(toClickable(KitCustomItem.METEORIC_IRON_HELMET), 6);
        inventory.setSlot(toClickable(KitCustomItem.METEORIC_IRON_CHESTPLATE), 15);
        inventory.setSlot(toClickable(KitCustomItem.METEORIC_IRON_LEGGINGS), 24);
        inventory.setSlot(toClickable(KitCustomItem.METEORIC_IRON_BOOTS), 33);

        inventory.setSlot(toClickable(KitItem.TURTLE_HELMET), 7);
        inventory.setSlot(toClickable(KitItem.ELYTRA), 16);
    }
}
