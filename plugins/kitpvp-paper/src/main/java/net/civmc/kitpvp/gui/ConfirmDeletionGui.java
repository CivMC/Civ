package net.civmc.kitpvp.gui;

import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.kit.Kit;
import net.civmc.kitpvp.kit.KitPvpDao;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.FastMultiPageView;

public class ConfirmDeletionGui {

    public ConfirmDeletionGui(KitPvpDao dao, Player player, Kit kit, FastMultiPageView parent) {
        ClickableInventory inventory = new ClickableInventory(27, "Delete " + kit.name());
        inventory.setOnClose(parent::showScreen);

        ItemStack question = new ItemStack(Material.PAPER);
        ItemMeta questionMeta = question.getItemMeta();
        questionMeta.itemName(Component.text("Are you sure you want to delete kit: %s?".formatted(kit.name())));
        question.setItemMeta(questionMeta);
        inventory.setItem(question, 13);

        ItemStack yes = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta yesMeta = yes.getItemMeta();
        yesMeta.itemName(Component.text("Yes, delete kit", NamedTextColor.GREEN));
        yes.setItemMeta(yesMeta);
        inventory.setSlot(new Clickable(yes) {
            @Override
            protected void clicked(@NotNull Player clicker) {
                Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
                    dao.deleteKit(kit.id());
                });
                clicker.sendMessage(Component.text("Deleted kit: %s".formatted(kit.name()), NamedTextColor.GREEN));
                inventory.setOnClose(null);
                clicker.closeInventory();
            }
        }, 10);

        ItemStack no = new ItemStack(Material.RED_CONCRETE);
        ItemMeta noMeta = yes.getItemMeta();
        noMeta.itemName(Component.text("No, cancel deleting kit", NamedTextColor.RED));
        no.setItemMeta(noMeta);
        inventory.setSlot(new Clickable(no) {
            @Override
            protected void clicked(@NotNull Player clicker) {
                inventory.setOnClose(null);
                parent.showScreen();
            }
        }, 16);
        inventory.showInventory(player);
    }
}
