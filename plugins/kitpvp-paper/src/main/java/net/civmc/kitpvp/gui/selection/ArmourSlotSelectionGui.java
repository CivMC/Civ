package net.civmc.kitpvp.gui.selection;

import net.civmc.kitpvp.kit.Kit;
import net.civmc.kitpvp.kit.KitCustomItem;
import net.civmc.kitpvp.kit.KitItem;
import net.civmc.kitpvp.kit.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import net.civmc.kitpvp.kit.KitCost;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import java.util.List;

public class ArmourSlotSelectionGui extends ItemSelectionGui {

    private final List<Material> items;
    private final List<String> custom;

    public ArmourSlotSelectionGui(KitPvpDao dao, Player player, int slot, Kit kit, EditKitGui gui, List<Material> items, List<String> custom) {
        super(dao, "Armour", player, slot, kit, gui::open, gui);
        this.items = items;
        this.custom = custom;
    }

    @Override
    public void addItems(ClickableInventory inventory) {
        ItemStack none = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta noneMeta = none.getItemMeta();
        noneMeta.itemName(Component.text("None", NamedTextColor.GRAY));
        none.setItemMeta(noneMeta);
        inventory.setSlot(toClickable(none, null), 18);

        for (int i = 0; i < items.size(); i++) {
            for (KitItem item : KitItem.values()) {
                if (item.getItem() == items.get(i)) {
                    ItemStack stack = new ItemStack(items.get(i));
                    inventory.setSlot(toClickable(KitCost.setPoints(stack, item.getCost()), stack), 19 + i);
                    break;
                }
            }
        }
        for (int i = 0; i < custom.size(); i++) {
            for (KitCustomItem item : KitCustomItem.values()) {
                if (item.getItem().equals(custom.get(i))) {
                    ItemStack stack = CustomItem.getCustomItem(custom.get(i));
                    inventory.setSlot(toClickable(KitCost.setPoints(stack, item.getCost()), stack), 19 + items.size() + i);
                    break;
                }
            }
        }
    }
}
