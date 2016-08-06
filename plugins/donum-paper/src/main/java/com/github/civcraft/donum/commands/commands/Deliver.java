package com.github.civcraft.donum.commands.commands;

import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;

import com.github.civcraft.donum.Donum;
import com.github.civcraft.donum.gui.DeliveryGUI;
import com.github.civcraft.donum.inventories.DeliveryInventory;

public class Deliver extends PlayerCommand {

	public Deliver(String name) {
		super(name);
		setIdentifier("deliver");
		setDescription("Opens an inventory to which you can add items to forward them to the players delivery inventory");
		setUsage("/deliver <playerName>");
		setArguments(1, 1);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Please no");
			return true;
		}
		UUID uuid = ((Player) sender).getUniqueId();
		DeliveryInventory delInv = Donum.getManager().getDeliveryInventory(uuid);
		if (delInv == null) {
			sender.sendMessage(ChatColor.RED + "Your inventory isnt loaded yet, try again in a few seconds");
			return true;
		}
		DeliveryGUI delGUI = new DeliveryGUI(uuid, delInv);
		delGUI.showScreen();
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return null;
	}
}
