package net.civmc.kitpvp.arena.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import java.util.List;
import net.civmc.kitpvp.arena.ArenaManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;

public class ConfirmDeletionGui {

    public ConfirmDeletionGui(ArenaManager manager, PlayerProfile owner, Player player) {
        ClickableInventory inventory = new ClickableInventory(27, "Delete arena");

        ItemStack question = new ItemStack(Material.PAPER);
        ItemMeta questionMeta = question.getItemMeta();
        questionMeta.itemName(Component.text("Are you sure you want to delete your arena?"));
        questionMeta.lore(List.of(Component.text("All players will be sent to spawn", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)));
        question.setItemMeta(questionMeta);
        inventory.setItem(question, 13);

        ItemStack yes = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta yesMeta = yes.getItemMeta();
        yesMeta.itemName(Component.text("Yes, delete arena", NamedTextColor.GREEN));
        yes.setItemMeta(yesMeta);
        inventory.setSlot(new Clickable(yes) {
            @Override
            protected void clicked(@NotNull Player clicker) {
                manager.deleteArena(owner);
                clicker.sendMessage(Component.text("Deleted arena.", NamedTextColor.GREEN));
                clicker.closeInventory();
            }
        }, 10);

        ItemStack no = new ItemStack(Material.RED_CONCRETE);
        ItemMeta noMeta = yes.getItemMeta();
        noMeta.itemName(Component.text("No, cancel deleting arena", NamedTextColor.RED));
        no.setItemMeta(noMeta);
        inventory.setSlot(new Clickable(no) {
            @Override
            protected void clicked(@NotNull Player clicker) {
                clicker.closeInventory();
            }
        }, 16);
        inventory.showInventory(player);
    }
}
