package net.civmc.kitpvp.gui.selection;

import net.civmc.kitpvp.kit.Kit;
import net.civmc.kitpvp.kit.KitCategory;
import net.civmc.kitpvp.kit.KitItem;
import net.civmc.kitpvp.kit.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import net.civmc.kitpvp.kit.KitCost;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;

public class FoodSelectionGui extends ItemSelectionGui {

    public FoodSelectionGui(KitPvpDao dao, Player player, int slot, Kit kit, Runnable parent, EditKitGui gui) {
        super(dao, "Food", player, slot, kit, parent, gui);
    }

    @Override
    public void addItems(ClickableInventory inventory) {
        int slot = 0;
        for (KitItem item : KitItem.getItems(KitCategory.FOOD)) {
            ItemStack stack = new ItemStack(item.getItem());
            if (item.getCost() != 0) {
                inventory.setSlot(toClickable(KitCost.setPoints(stack, item.getCost()), stack), slot);
            } else {
                inventory.setSlot(toClickable(stack), slot);
            }
            slot++;
        }
    }
}
