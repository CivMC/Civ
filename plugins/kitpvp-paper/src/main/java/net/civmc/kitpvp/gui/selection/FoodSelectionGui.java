package net.civmc.kitpvp.gui.selection;

import net.civmc.kitpvp.dao.Kit;
import net.civmc.kitpvp.dao.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;

public class FoodSelectionGui extends ItemSelectionGui {

    public FoodSelectionGui(KitPvpDao dao, String title, Player player, int slot, Kit kit, Runnable parent, EditKitGui gui) {
        super(dao, title, player, slot, kit, parent, gui);
    }

    @Override
    public void addItems(ClickableInventory inventory) {
        inventory.setSlot(toClickable(new ItemStack(Material.COOKED_PORKCHOP)), 1);
        inventory.setSlot(toClickable(new ItemStack(Material.GOLDEN_APPLE)), 2);
        inventory.setSlot(toClickable(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE)), 3);
        inventory.setSlot(toClickable(new ItemStack(Material.COOKED_BEEF)), 4);
        inventory.setSlot(toClickable(new ItemStack(Material.GOLDEN_CARROT)), 5);
        inventory.setSlot(toClickable(new ItemStack(Material.SWEET_BERRIES)), 6);
        inventory.setSlot(toClickable(new ItemStack(Material.PUMPKIN_PIE)), 7);
        inventory.setSlot(toClickable(new ItemStack(Material.BREAD)), 8);
        inventory.setSlot(toClickable(new ItemStack(Material.COOKED_CHICKEN)), 9);
    }
}
