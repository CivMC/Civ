package com.github.maxopoly.finale.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.github.maxopoly.finale.Finale;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandAlias("finalereload")
@CommandPermission("finale.op")
public class ReloadFinaleCommand extends BaseCommand {

	@Syntax("/finalereload")
	@Description("Reloads finale entirely")
	public void execute(CommandSender sender, String[] arg1) {
		Finale.getPlugin().reload();
		sender.sendMessage(ChatColor.GREEN + "Reloaded Finale");
	}
}
