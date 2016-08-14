package com.github.civcraft.donum.commands.commands;

import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.NameAPI;

import com.github.civcraft.donum.Donum;
import com.github.civcraft.donum.gui.DeathInventoryGUI;
import com.github.civcraft.donum.inventories.DeathInventory;

public class DeliverDeath extends PlayerCommand {
	public DeliverDeath(String name) {
		super(name);
		setIdentifier("deliverdeath");
		setDescription("Shows death inventories for a player, by default the last 25");
		setUsage("/deliverdeath <playerName> [inventoriesToRetrieve]");
		setArguments(1, 2);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "No");
			return true;
		}
		UUID delUUID = NameAPI.getUUID(args[0]);
		if (delUUID == null) {
			sender.sendMessage(ChatColor.RED + "This player has never logged into civcraft");
			return true;
		}
		int amountToRetrieve;
		if (args.length >= 2) {
			try {
				amountToRetrieve = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + args[1] + " is not a valid number");
				return true;
			}
		}
		else {
			amountToRetrieve = 25;
		}
		List <DeathInventory> inventories = Donum.getManager().getDeathInventories(delUUID, amountToRetrieve);
		DeathInventoryGUI gui = new DeathInventoryGUI(((Player) sender).getUniqueId(), inventories);
		gui.showScreen();
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return null;
	}
}