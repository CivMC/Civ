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

public class WeaponsAndToolsSelectionGui extends ItemSelectionGui {

    public WeaponsAndToolsSelectionGui(KitPvpDao dao, Player player, int slot, Kit kit, Runnable parent, EditKitGui gui) {
        super(dao, "Weapons and Tools", player, slot, kit, parent, gui);
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
        inventory.setSlot(toClickable(KitItem.WOODEN_SWORD), 0);
        inventory.setSlot(toClickable(KitItem.WOODEN_AXE), 9);
        inventory.setSlot(toClickable(KitItem.WOODEN_PICKAXE), 18);
        inventory.setSlot(toClickable(KitItem.WOODEN_SHOVEL), 27);
        inventory.setSlot(toClickable(KitItem.WOODEN_HOE), 36);

        inventory.setSlot(toClickable(KitItem.GOLDEN_SWORD), 1);
        inventory.setSlot(toClickable(KitItem.GOLDEN_AXE), 10);
        inventory.setSlot(toClickable(KitItem.GOLDEN_PICKAXE), 19);
        inventory.setSlot(toClickable(KitItem.GOLDEN_SHOVEL), 28);
        inventory.setSlot(toClickable(KitItem.GOLDEN_HOE), 37);

        inventory.setSlot(toClickable(KitItem.STONE_SWORD), 2);
        inventory.setSlot(toClickable(KitItem.STONE_AXE), 11);
        inventory.setSlot(toClickable(KitItem.STONE_PICKAXE), 20);
        inventory.setSlot(toClickable(KitItem.STONE_SHOVEL), 29);
        inventory.setSlot(toClickable(KitItem.STONE_HOE), 38);

        inventory.setSlot(toClickable(KitItem.IRON_SWORD), 3);
        inventory.setSlot(toClickable(KitItem.IRON_AXE), 12);
        inventory.setSlot(toClickable(KitItem.IRON_PICKAXE), 21);
        inventory.setSlot(toClickable(KitItem.IRON_SHOVEL), 30);
        inventory.setSlot(toClickable(KitItem.IRON_HOE), 39);

        inventory.setSlot(toClickable(KitItem.DIAMOND_SWORD), 4);
        inventory.setSlot(toClickable(KitItem.DIAMOND_AXE), 13);
        inventory.setSlot(toClickable(KitItem.DIAMOND_PICKAXE), 22);
        inventory.setSlot(toClickable(KitItem.DIAMOND_SHOVEL), 31);
        inventory.setSlot(toClickable(KitItem.DIAMOND_HOE), 40);

        inventory.setSlot(toClickable(KitItem.NETHERITE_SWORD), 5);
        inventory.setSlot(toClickable(KitItem.NETHERITE_AXE), 14);
        inventory.setSlot(toClickable(KitItem.NETHERITE_PICKAXE), 23);
        inventory.setSlot(toClickable(KitItem.NETHERITE_SHOVEL), 32);
        inventory.setSlot(toClickable(KitItem.NETHERITE_HOE), 41);

        inventory.setSlot(toClickable(KitItem.SHIELD), 6);
        inventory.setSlot(toClickable(KitItem.TRIDENT), 7);
        inventory.setSlot(toClickable(KitItem.FLINT_AND_STEEL), 8);

        inventory.setSlot(toClickable(KitItem.SHEARS), 15);
        inventory.setSlot(toClickable(KitItem.ENDER_PEARL), 16);
        inventory.setSlot(toClickable(KitItem.FIREWORK_ROCKET), 17);

        inventory.setSlot(toClickable(KitItem.BUCKET), 24);
        inventory.setSlot(toClickable(KitItem.WATER_BUCKET), 25);
        inventory.setSlot(toClickable(KitItem.LAVA_BUCKET), 26);

        inventory.setSlot(toClickable(KitItem.MILK_BUCKET), 33);
        inventory.setSlot(toClickable(KitItem.POWDER_SNOW_BUCKET), 34);
        inventory.setSlot(toClickable(KitItem.ARROW), 35);

        inventory.setSlot(toClickable(KitItem.BOW), 42);
        inventory.setSlot(toClickable(KitItem.CROSSBOW), 43);
        inventory.setSlot(toClickable(KitItem.FISHING_ROD), 44);

        inventory.setSlot(toClickable(KitCustomItem.METEORIC_IRON_SWORD), 46);
        inventory.setSlot(toClickable(KitCustomItem.METEORIC_IRON_SWORD_KNOCKBACK1), 47);
        inventory.setSlot(toClickable(KitCustomItem.METEORIC_IRON_SWORD_KNOCKBACK), 48);
        inventory.setSlot(toClickable(KitCustomItem.METEORIC_IRON_AXE), 49);
        inventory.setSlot(toClickable(KitCustomItem.METEORIC_IRON_PICKAXE), 50);
        inventory.setSlot(toClickable(KitCustomItem.BACKPACK), 51);
        inventory.setSlot(toClickable(KitItem.OAK_BOAT), 52);
        inventory.setSlot(toClickable(KitItem.TNT_MINECART), 53);
    }
}
