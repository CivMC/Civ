package com.github.civcraft.donum.commands.commands;

import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.NameAPI;

import com.github.civcraft.donum.gui.AdminDeliveryGUI;
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
			sender.sendMessage(ChatColor.RED + "No");
			return true;
		}
		//TODO make namelayer soft dependency
		UUID delUUID = NameAPI.getUUID(args [0]);
		if (delUUID == null) {
			sender.sendMessage(ChatColor.RED + "This player has never logged into civcraft");
			return true;
		}
		AdminDeliveryGUI.showInventory((Player) sender, delUUID);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return null;
	}
}
