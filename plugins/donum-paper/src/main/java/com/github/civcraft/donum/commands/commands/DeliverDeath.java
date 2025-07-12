package com.github.civcraft.donum.commands.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.github.civcraft.donum.Donum;
import com.github.civcraft.donum.gui.DeathInventoryGUI;
import com.github.civcraft.donum.inventories.DeathInventory;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;

public class DeliverDeath extends BaseCommand {

    @CommandAlias("deliverdeath")
    @Syntax("<player> [inventoriesToGet]")
    @Description("Shows death inventories for a player, by default the last 25")
    @CommandCompletion("@players")
    @CommandPermission("donum.op")
    public void execute(Player player, String targetPlayer, @Optional String inventoriesToGet) {
        UUID delUUID = NameAPI.getUUID(targetPlayer);
        if (delUUID == null) {
            player.sendMessage(ChatColor.RED + "This player has never logged into civcraft");
            return;
        }
        int amountToRetrieve;
        if (inventoriesToGet != null) {
            try {
                amountToRetrieve = Integer.parseInt(inventoriesToGet);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + inventoriesToGet + " is not a valid number");
                return;
            }
        } else {
            amountToRetrieve = 25;
        }
        List<DeathInventory> inventories = Donum.getManager().getDeathInventories(delUUID, amountToRetrieve);
        DeathInventoryGUI gui = new DeathInventoryGUI(player.getUniqueId(), inventories);
        gui.showScreen();
    }
}
