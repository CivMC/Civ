package net.civmc.kitpvp.gui.selection;

import net.civmc.kitpvp.data.Kit;
import net.civmc.kitpvp.data.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;

public class ItemCategorySelectionGui extends ItemSelectionGui {

    private final KitPvpDao dao;
    private final Player player;
    private final int slot;
    private final Kit kit;
    private final EditKitGui gui;

    public ItemCategorySelectionGui(KitPvpDao dao, Player player, int slot, Kit kit, EditKitGui gui) {
        super(dao, "Item categories", player, slot, kit, gui::open, gui);
        this.dao = dao;
        this.player = player;
        this.slot = slot;
        this.kit = kit;
        this.gui = gui;
    }

    public void addItems(ClickableInventory inventory) {
        ItemStack none = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta noneMeta = none.getItemMeta();
        noneMeta.itemName(Component.text("None", NamedTextColor.GOLD));
        none.setItemMeta(noneMeta);
        inventory.setSlot(toClickable(none, null), 18);

        inventory.setSlot(getSlot(inventory, new ItemStack(Material.IRON_CHESTPLATE), "Armour",
                () -> new GenericArmourSelectionGui(dao, player, slot, kit, this::open, gui).open()),
            19);

        inventory.setSlot(getSlot(inventory, new ItemStack(Material.IRON_SWORD), "Weapons and Tools",
                () -> new WeaponsAndToolsSelectionGui(dao, player, slot, kit, this::open, gui).open()),
            20);

        ItemStack potions = new ItemStack(Material.POTION);
        PotionMeta potionsMeta = (PotionMeta) potions.getItemMeta();
        potionsMeta.setColor(Color.YELLOW);
        potionsMeta.displayName(Component.text("Potions", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        potions.setItemMeta(potionsMeta);
        // item name doesn't work on potion
        inventory.setSlot(getSlot(inventory, potions, "Potions",
                () -> new PotionsSelectionGui(dao, player, slot, kit, this::open, gui).open()),
            21);

        inventory.setSlot(getSlot(inventory, new ItemStack(Material.GOLDEN_CARROT), "Food",
                () -> new FoodSelectionGui(dao, player, slot, kit, this::open, gui).open()),
            22);

        inventory.setSlot(getSlot(inventory, new ItemStack(Material.COBBLESTONE), "Blocks",
                () -> new BlockSelectionGui(dao, player, slot, kit, this::open, gui).open()),
            23);

        if (Bukkit.getPluginManager().isPluginEnabled("BreweryX")) {
            ItemStack drugs = new ItemStack(Material.POTION);
            PotionMeta drugsMeta = (PotionMeta) drugs.getItemMeta();
            drugsMeta.setColor(Color.fromRGB(0x97, 0xFF, 0xFF));
            drugsMeta.displayName(Component.text("Drugs", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            drugs.setItemMeta(drugsMeta);
            inventory.setSlot(getSlot(inventory, drugs, "Drugs",
                    () -> new DrugsSelectionGui(dao, player, slot, kit, this::open, gui).open()),
                24);
        }
    }

    private Clickable getSlot(ClickableInventory inventory, ItemStack item, String name, Runnable gui) {
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.itemName(Component.text(name, NamedTextColor.GOLD));
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
