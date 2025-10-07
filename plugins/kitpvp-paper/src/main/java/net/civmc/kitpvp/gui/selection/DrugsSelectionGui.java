package net.civmc.kitpvp.gui.selection;

import com.dre.brewery.Brew;
import com.dre.brewery.recipe.BRecipe;
import net.civmc.kitpvp.kit.Kit;
import net.civmc.kitpvp.kit.KitDrugs;
import net.civmc.kitpvp.kit.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import net.civmc.kitpvp.kit.KitCost;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;

public class DrugsSelectionGui extends ItemSelectionGui {

    public DrugsSelectionGui(KitPvpDao dao, Player player, int slot, Kit kit, Runnable parent, EditKitGui gui) {
        super(dao, "Drugs", player, slot, kit, parent, gui);
    }

    @Override
    public void addItems(ClickableInventory inventory) {
        int slot = 0;
        for (KitDrugs drug : KitDrugs.values()) {
            BRecipe matching = BRecipe.getMatching(drug.getBrew());
            if (matching != null) {
                Brew brew = matching.createBrew(10);
                ItemStack item = brew.createItem(matching);
                brew.seal(item, null);

                inventory.setSlot(toClickable(KitCost.setPoints(item, drug.getCost()), item), slot++);
            }
        }
    }
}
