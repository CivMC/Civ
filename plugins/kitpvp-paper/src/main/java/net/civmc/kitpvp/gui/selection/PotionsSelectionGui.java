package net.civmc.kitpvp.gui.selection;

import net.civmc.kitpvp.dao.Kit;
import net.civmc.kitpvp.dao.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;

public class PotionsSelectionGui extends ItemSelectionGui {

    public PotionsSelectionGui(KitPvpDao dao, String title, Player player, int slot, Kit kit, Runnable parent, EditKitGui gui) {
        super(dao, title, player, slot, kit, parent, gui);
    }

    @Override
    public void addItems(ClickableInventory inventory) {

    }
}
