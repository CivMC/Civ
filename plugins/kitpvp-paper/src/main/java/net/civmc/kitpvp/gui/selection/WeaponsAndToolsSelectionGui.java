package net.civmc.kitpvp.gui.selection;

import net.civmc.kitpvp.data.Kit;
import net.civmc.kitpvp.data.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;

public class WeaponsAndToolsSelectionGui extends ItemSelectionGui {

    public WeaponsAndToolsSelectionGui(KitPvpDao dao, Player player, int slot, Kit kit, Runnable parent, EditKitGui gui) {
        super(dao, "Weapons and Tools", player, slot, kit, parent, gui);
    }

    @Override
    public void addItems(ClickableInventory inventory) {
        inventory.setSlot(toClickable(new ItemStack(Material.WOODEN_SWORD)), 0);
        inventory.setSlot(toClickable(new ItemStack(Material.WOODEN_AXE)), 9);
        inventory.setSlot(toClickable(new ItemStack(Material.WOODEN_PICKAXE)), 18);
        inventory.setSlot(toClickable(new ItemStack(Material.WOODEN_SHOVEL)), 27);
        inventory.setSlot(toClickable(new ItemStack(Material.WOODEN_HOE)), 36);

        inventory.setSlot(toClickable(new ItemStack(Material.GOLDEN_SWORD)), 1);
        inventory.setSlot(toClickable(new ItemStack(Material.GOLDEN_AXE)), 10);
        inventory.setSlot(toClickable(new ItemStack(Material.GOLDEN_PICKAXE)), 19);
        inventory.setSlot(toClickable(new ItemStack(Material.GOLDEN_SHOVEL)), 28);
        inventory.setSlot(toClickable(new ItemStack(Material.GOLDEN_HOE)), 37);

        inventory.setSlot(toClickable(new ItemStack(Material.STONE_SWORD)), 2);
        inventory.setSlot(toClickable(new ItemStack(Material.STONE_AXE)), 11);
        inventory.setSlot(toClickable(new ItemStack(Material.STONE_PICKAXE)), 20);
        inventory.setSlot(toClickable(new ItemStack(Material.STONE_SHOVEL)), 29);
        inventory.setSlot(toClickable(new ItemStack(Material.STONE_HOE)), 38);

        inventory.setSlot(toClickable(new ItemStack(Material.IRON_SWORD)), 3);
        inventory.setSlot(toClickable(new ItemStack(Material.IRON_AXE)), 12);
        inventory.setSlot(toClickable(new ItemStack(Material.IRON_PICKAXE)), 21);
        inventory.setSlot(toClickable(new ItemStack(Material.IRON_SHOVEL)), 30);
        inventory.setSlot(toClickable(new ItemStack(Material.IRON_HOE)), 39);

        inventory.setSlot(toClickable(new ItemStack(Material.DIAMOND_SWORD)), 4);
        inventory.setSlot(toClickable(new ItemStack(Material.DIAMOND_AXE)), 13);
        inventory.setSlot(toClickable(new ItemStack(Material.DIAMOND_PICKAXE)), 22);
        inventory.setSlot(toClickable(new ItemStack(Material.DIAMOND_SHOVEL)), 31);
        inventory.setSlot(toClickable(new ItemStack(Material.DIAMOND_HOE)), 40);

        inventory.setSlot(toClickable(new ItemStack(Material.NETHERITE_SWORD)), 5);
        inventory.setSlot(toClickable(new ItemStack(Material.NETHERITE_AXE)), 14);
        inventory.setSlot(toClickable(new ItemStack(Material.NETHERITE_PICKAXE)), 23);
        inventory.setSlot(toClickable(new ItemStack(Material.NETHERITE_SHOVEL)), 32);
        inventory.setSlot(toClickable(new ItemStack(Material.NETHERITE_HOE)), 41);

        inventory.setSlot(toClickable(new ItemStack(Material.SHIELD)), 6);
        inventory.setSlot(toClickable(new ItemStack(Material.TRIDENT)), 7);
        inventory.setSlot(toClickable(new ItemStack(Material.FLINT_AND_STEEL)), 8);

        inventory.setSlot(toClickable(new ItemStack(Material.SHEARS)), 15);
        inventory.setSlot(toClickable(new ItemStack(Material.ENDER_PEARL)), 16);
        inventory.setSlot(toClickable(new ItemStack(Material.FIREWORK_ROCKET)), 17);

        inventory.setSlot(toClickable(new ItemStack(Material.BUCKET)), 24);
        inventory.setSlot(toClickable(new ItemStack(Material.WATER_BUCKET)), 25);
        inventory.setSlot(toClickable(new ItemStack(Material.LAVA_BUCKET)), 26);

        inventory.setSlot(toClickable(new ItemStack(Material.MILK_BUCKET)), 33);
        inventory.setSlot(toClickable(new ItemStack(Material.POWDER_SNOW_BUCKET)), 34);
        inventory.setSlot(toClickable(new ItemStack(Material.ARROW)), 35);

        inventory.setSlot(toClickable(new ItemStack(Material.BOW)), 42);
        inventory.setSlot(toClickable(new ItemStack(Material.CROSSBOW)), 43);
        inventory.setSlot(toClickable(new ItemStack(Material.FISHING_ROD)), 44);

        inventory.setSlot(toClickable(CustomItem.getCustomItem("meteoric_iron_sword")), 47);
        inventory.setSlot(toClickable(CustomItem.getCustomItem("meteoric_iron_sword_knockback")), 48);
        inventory.setSlot(toClickable(CustomItem.getCustomItem("meteoric_iron_axe")), 49);
        inventory.setSlot(toClickable(CustomItem.getCustomItem("meteoric_iron_pickaxe")), 50);
        inventory.setSlot(toClickable(CustomItem.getCustomItem("backpack")), 51);
        inventory.setSlot(toClickable(new ItemStack(Material.OAK_BOAT)), 52);
        inventory.setSlot(toClickable(new ItemStack(Material.TNT_MINECART)), 53);
    }
}
