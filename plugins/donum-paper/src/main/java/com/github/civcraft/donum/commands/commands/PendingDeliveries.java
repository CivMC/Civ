package com.github.civcraft.donum.commands.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.github.civcraft.donum.Donum;
import com.github.civcraft.donum.gui.AdminDeliveryGUI;
import com.github.civcraft.donum.inventories.DeliveryInventory;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import vg.civcraft.mc.namelayer.NameAPI;

public class PendingDeliveries extends BaseCommand {

    @CommandAlias("pendingdeliveries|opendeliveries")
    @Description("Opens an inventory to show which items have been sent to the player's delivery inventory")
    @Syntax("<player>")
    @CommandCompletion("@players")
    @CommandPermission("donum.op")
    public void execute(Player player, String targetPlayer) {
        UUID delUUID = NameAPI.getUUID(targetPlayer);
        if (delUUID == null) {
            player.sendMessage(ChatColor.RED + "This player has never logged into the server");
            return;
        }

        DeliveryInventory delInv = Donum.getManager().getDeliveryInventory(delUUID);
        if (delInv == null) {
            player.sendMessage(ChatColor.RED + "Player inventory isn't loaded yet, try again in a few seconds");
            return;
        }

        Inventory inventory = Bukkit.createInventory(null, 54, NameAPI.getCurrentName(delUUID));
        // add items to ui
        delInv.getInventory().getItemStackRepresentation().forEach(inventory::addItem);
        player.openInventory(inventory);
    }
}
