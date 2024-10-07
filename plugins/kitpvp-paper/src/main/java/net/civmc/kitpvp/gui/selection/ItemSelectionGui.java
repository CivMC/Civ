package net.civmc.kitpvp.gui.selection;

import net.civmc.kitpvp.dao.Kit;
import net.civmc.kitpvp.dao.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.LClickable;

public abstract class ItemSelectionGui {
    private final KitPvpDao dao;
    private final String title;
    private final Player player;
    private final Runnable parent;
    private final EditKitGui gui;
    private final int slot;
    private final Kit kit;


    public ItemSelectionGui(KitPvpDao dao, String title, Player player, int slot, Kit kit, Runnable parent, EditKitGui gui) {
        this.dao = dao;
        this.title = title;
        this.player = player;
        this.parent = parent;
        this.gui = gui;
        this.slot = slot;
        this.kit = kit;
    }

    public void open() {
        ClickableInventory inventory = new ClickableInventory(54, this.title);

        addItems(inventory);

        inventory.setOnClose(parent);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.itemName(Component.text("Back", NamedTextColor.GRAY));
        back.setItemMeta(backMeta);
        inventory.setSlot(new LClickable(back, HumanEntity::closeInventory), 45);

        inventory.showInventory(player);
    }

    public abstract void addItems(ClickableInventory inventory);

    protected Clickable toClickable(ItemStack item) {
        return new Clickable(item) {
            @Override
            protected void clicked(@NotNull Player clicker) {
                ItemStack[] items = kit.items().clone();
                items[slot] = item;

                Kit updatedKit = dao.updateKit(kit.id(), kit.icon(), items);
                gui.updateKit(updatedKit);
                gui.open();
            }
        };
    }
}
