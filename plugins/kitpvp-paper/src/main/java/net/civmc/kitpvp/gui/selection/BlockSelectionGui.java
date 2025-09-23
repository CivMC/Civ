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

import java.util.List;

public class BlockSelectionGui extends ItemSelectionGui {

    public BlockSelectionGui(KitPvpDao dao, Player player, int slot, Kit kit, Runnable parent, EditKitGui gui) {
        super(dao, "Blocks", player, slot, kit, parent, gui);
    }

    @Override
    public void addItems(ClickableInventory inventory) {
        List<KitItem> items = KitItem.getItems(KitCategory.BLOCK);
        for (int i = 0; i < items.size(); i++) {
            KitItem item = items.get(i);
            ItemStack created = new ItemStack(item.getItem());
            inventory.setSlot(toClickable(KitCost.setPoints(created, item.getCost()), created), i);
        }
    }
}
