package com.github.maxopoly.KiraBukkitGateway.command.commands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class ReloadKiraCommand extends PlayerCommand {

	public ReloadKiraCommand() {
		super("kirareload");
		setIdentifier("kirareload");
		setDescription("Reloads KiraBukkitGateway");
		setUsage("/kirareload");
		setArguments(0, 0);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		KiraBukkitGatewayPlugin.getInstance().reload();
		sender.sendMessage(ChatColor.GREEN + "Reloaded KiraBukkitGateway");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new LinkedList<>();
	}

}
