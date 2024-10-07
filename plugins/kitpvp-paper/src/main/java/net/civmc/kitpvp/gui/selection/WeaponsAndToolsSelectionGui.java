package net.civmc.kitpvp.gui.selection;

import net.civmc.kitpvp.dao.Kit;
import net.civmc.kitpvp.dao.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;

public class WeaponsAndToolsSelectionGui extends ItemSelectionGui {

    public WeaponsAndToolsSelectionGui(KitPvpDao dao, String title, Player player, int slot, Kit kit, Runnable parent, EditKitGui gui) {
        super(dao, title, player, slot, kit, parent, gui);
    }

    @Override
    public void addItems(ClickableInventory inventory) {
        inventory.setSlot(toClickable(new ItemStack(Material.WOODEN_SWORD)), 9);
        inventory.setSlot(toClickable(new ItemStack(Material.GOLDEN_SWORD)), 10);
        inventory.setSlot(toClickable(new ItemStack(Material.STONE_SWORD)), 11);
        inventory.setSlot(toClickable(new ItemStack(Material.IRON_SWORD)), 12);
        inventory.setSlot(toClickable(new ItemStack(Material.DIAMOND_SWORD)), 13);
        inventory.setSlot(toClickable(new ItemStack(Material.NETHERITE_SWORD)), 14);

        inventory.setSlot(toClickable(new ItemStack(Material.SHIELD)), 18);
        inventory.setSlot(toClickable(new ItemStack(Material.TRIDENT)), 19);
        inventory.setSlot(toClickable(new ItemStack(Material.FISHING_ROD)), 20);
        inventory.setSlot(toClickable(new ItemStack(Material.ENDER_PEARL)), 21);
        inventory.setSlot(toClickable(new ItemStack(Material.MILK_BUCKET)), 22);
        inventory.setSlot(toClickable(new ItemStack(Material.SNOWBALL)), 23);
    }
}
