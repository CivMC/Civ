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

import java.util.List;

public class ItemCategorySelectionGui {

    private final KitPvpDao dao;
    private final Player player;
    private final int slot;
    private final Kit kit;
    private final EditKitGui gui;

    public ItemCategorySelectionGui(KitPvpDao dao, Player player, int slot, Kit kit, EditKitGui gui) {
        this.dao = dao;
        this.player = player;
        this.slot = slot;
        this.kit = kit;
        this.gui = gui;
    }

    public void open() {
        ClickableInventory inventory = new ClickableInventory(54, "Item categories");

        inventory.setOnClose(gui::open);

        ItemStack none = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta noneMeta = none.getItemMeta();
        noneMeta.itemName(Component.text("None", NamedTextColor.GOLD));
        none.setItemMeta(noneMeta);
        inventory.setSlot(new LClickable(none, p -> gui.open()), 18);

        inventory.setSlot(getSlot(inventory, kit, slot, Material.IRON_CHESTPLATE, "Armour",
                () -> new GenericArmourSelectionGui(dao, "Armour", player, slot, kit, this::open, gui)),
            19);

        inventory.setSlot(getSlot(inventory, kit, slot, Material.IRON_CHESTPLATE, "Weapons and Tools",
                () -> new WeaponsAndToolsSelectionGui(dao, "Weapons and Tools", player, slot, kit, this::open, gui)),
            20);

        inventory.setSlot(getSlot(inventory, kit, slot, Material.POTION, "Potions",
                () -> new PotionsSelectionGui(dao, "Potions", player, slot, kit, this::open, gui)),
            21);

        inventory.setSlot(getSlot(inventory, kit, slot, Material.GOLDEN_CARROT, "Food",
                () -> new FoodSelectionGui(dao, "Food", player, slot, kit, this::open, gui)),
            22);



        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.itemName(Component.text("Back", NamedTextColor.GRAY));
        back.setItemMeta(backMeta);
        inventory.setSlot(new LClickable(none, HumanEntity::closeInventory), 45);
    }

    private Clickable getSlot(ClickableInventory inventory, Kit kit, int itemIndex, Material icon, String name, Runnable gui) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text(name, NamedTextColor.GRAY));
        meta.lore(List.of(Component.text(name, NamedTextColor.YELLOW)));
        item.setItemMeta(meta);

        return new Clickable(item) {
            @Override
            protected void clicked(@NotNull Player clicker) {
                inventory.setOnClose(null);
                gui.run();
            }
        };
    }
}
