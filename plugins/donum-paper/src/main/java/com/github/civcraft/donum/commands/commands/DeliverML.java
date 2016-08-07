package com.github.civcraft.donum.commands.commands;

import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import com.github.civcraft.donum.DonumAPI;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.namelayer.NameAPI;

public class DeliverML extends PlayerCommand {
	public DeliverML(String name) {
		super(name);
		setIdentifier("deliverml");
		setDescription("Converts logs into item returns");
		setUsage("/deliverml <playerName> [items]");
		setArguments(1, 1000);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		UUID delUUID = NameAPI.getUUID(args[0]);
		if (delUUID == null) {
			sender.sendMessage(ChatColor.RED + "This player has never logged into civcraft");
			return true;
		}
		int i = 1;
		ItemMap items = new ItemMap();
		while (i < args.length) {
			int amount;
			try {
				amount = Integer.valueOf(args[i]);
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + args [i] + " is not a valid number");
				continue;
			}
			i++;
			if (args.length <= i) {
				sender.sendMessage("Found no material for count at end of message");
				break;
			}
			String matString = args[i];
			i++;
			if (matString.endsWith("+")) {
				sender.sendMessage(ChatColor.RED + "Skipped " + amount + " "
						+ matString.substring(0, matString.length() - 1)
						+ ", because it was lored and could not be reconstructed properly");
				continue;
			}
			Material mat;
			try {
				mat = Material.valueOf(matString);
			} catch (IllegalArgumentException e) {
				sender.sendMessage(ChatColor.RED + "Skipped " + amount + " " + matString
						+ ", because it could not be parsed as valid material");
				continue;
			}
			ItemStack is = new ItemStack(mat,amount);
			items.addItemStack(is);
		}
		sender.sendMessage(ChatColor.GREEN + "Added a total of " + items.getTotalItemAmount() + " items to the delivery inventory of " + NameAPI.getCurrentName(delUUID));
		DonumAPI.deliverItem(delUUID, items);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return null;
	}
}
