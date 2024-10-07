package net.civmc.kitpvp.gui;

import net.civmc.kitpvp.dao.Kit;
import net.civmc.kitpvp.dao.KitPvpDao;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.FastMultiPageView;

public class ConfirmDeletionGui {

    private final KitPvpDao dao;
    private final Player player;

    public ConfirmDeletionGui(KitPvpDao dao, Player player, Kit kit, FastMultiPageView parent) {
        this.dao = dao;
        this.player = player;
        ClickableInventory inventory = new ClickableInventory(36, "Confirm deletion");
        ItemStack question = new ItemStack(Material.PAPER);
        ItemMeta questionMeta = question.getItemMeta();
        questionMeta.itemName(Component.text("Are you sure you want to delete kit: %s?".formatted(kit.name())));
        question.setItemMeta(questionMeta);
        inventory.setItem(question, 13);

        ItemStack yes = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta yesMeta = yes.getItemMeta();
        yesMeta.itemName(Component.text("Yes, delete kit: %s".formatted(kit.name()), NamedTextColor.GREEN));
        inventory.setSlot(new Clickable(yes) {
            @Override
            protected void clicked(@NotNull Player clicker) {
                dao.deleteKit(kit.id());
                clicker.sendMessage(Component.text("Deleted kit: %s".formatted(kit.name()), NamedTextColor.GREEN));
                clicker.closeInventory();
            }
        }, 10);

        ItemStack no = new ItemStack(Material.RED_CONCRETE);
        ItemMeta noMeta = yes.getItemMeta();
        noMeta.itemName(Component.text("No, cancel deleting kit: %s".formatted(kit.name()), NamedTextColor.RED));
        inventory.setSlot(new Clickable(no) {
            @Override
            protected void clicked(@NotNull Player clicker) {
                parent.showScreen();
            }
        }, 16);
        inventory.showInventory(player);
    }
}
