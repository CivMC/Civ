package com.github.maxopoly.finale.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.github.maxopoly.finale.Finale;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ReloadFinaleCommand extends BaseCommand {

	@CommandAlias("finalereload")
	@CommandPermission("finale.op")
	@Description("Reloads finale entirely")
	public void execute(CommandSender sender) {
		Finale.getPlugin().reload();
		sender.sendMessage(ChatColor.GREEN + "Reloaded Finale");
	}
}
