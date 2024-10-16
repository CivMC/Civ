package com.github.civcraft.donum.commands.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.github.civcraft.donum.Donum;
import com.github.civcraft.donum.gui.DeliveryGUI;
import com.github.civcraft.donum.inventories.DeliveryInventory;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;


public class OpenDeliveries extends BaseCommand {

	@CommandAlias("present")
	@Description("Opens your delivery inventory")
	public void execute(Player player) {
		UUID uuid = player.getUniqueId();
		DeliveryInventory delInv = Donum.getManager().getDeliveryInventory(uuid);
		if (delInv == null) {
			player.sendMessage(ChatColor.RED + "Your inventory isnt loaded yet, try again in a few seconds");
			return;
		}
		DeliveryGUI delGUI = new DeliveryGUI(uuid, delInv);
		delGUI.showScreen();
	}
}
