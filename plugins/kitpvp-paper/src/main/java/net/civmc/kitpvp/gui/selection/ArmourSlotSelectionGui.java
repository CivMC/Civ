package net.civmc.kitpvp.gui.selection;

import net.civmc.kitpvp.data.Kit;
import net.civmc.kitpvp.data.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import java.util.List;

public class ArmourSlotSelectionGui extends ItemSelectionGui {

    private final List<Material> items;

    public ArmourSlotSelectionGui(KitPvpDao dao, Player player, int slot, Kit kit, EditKitGui gui, List<Material> items) {
        super(dao, "Armour", player, slot, kit, gui::open, gui);
        this.items = items;
    }

    @Override
    public void addItems(ClickableInventory inventory) {
        ItemStack none = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta noneMeta = none.getItemMeta();
        noneMeta.itemName(Component.text("None", NamedTextColor.GRAY));
        none.setItemMeta(noneMeta);
        inventory.setSlot(toClickable(none, null), 18);

        for (int i = 0; i < Math.min(items.size(), 8); i++) {
            inventory.setSlot(toClickable(new ItemStack(items.get(i))), 19 + i);
        }
    }
}
