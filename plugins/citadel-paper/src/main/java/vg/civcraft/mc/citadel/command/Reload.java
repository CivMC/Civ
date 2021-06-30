package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import vg.civcraft.mc.citadel.Citadel;

@CommandAlias("citadelreload")
@CommandPermission("citadel.admin")
public class Reload extends BaseCommand {

	@Syntax("/citadelreload")
	@Description("Reloads Citadel entirely")
	public void execute(CommandSender sender, String[] args) {
		Citadel.getInstance().reload();
		sender.sendMessage(ChatColor.GREEN + "Reloaded Citadel");
	}
}
