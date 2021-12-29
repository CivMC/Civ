package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import vg.civcraft.mc.citadel.Citadel;

public class Reload extends BaseCommand {

	@CommandAlias("citadelreload")
	@CommandPermission("citadel.admin")
	@Description("Reloads Citadel entirely")
	public void execute(CommandSender sender) {
		Citadel.getInstance().reload();
		sender.sendMessage(ChatColor.GREEN + "Reloaded Citadel");
	}
}
