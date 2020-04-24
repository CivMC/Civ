package com.github.maxopoly.KiraBukkitGateway.command;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;

import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "kirareload")
public class ReloadKiraCommand extends StandaloneCommand {

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
