package com.github.maxopoly.KiraBukkitGateway.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ReloadKiraCommand extends BaseCommand {

	@CommandAlias("kirareload")
	@CommandPermission("kira.op")
	@Description("Reloads KiraBukkitGateway")
	public void execute(CommandSender sender) {
		KiraBukkitGatewayPlugin.getInstance().reload();
		sender.sendMessage(ChatColor.GREEN + "Reloaded KiraBukkitGateway");
	}
}
