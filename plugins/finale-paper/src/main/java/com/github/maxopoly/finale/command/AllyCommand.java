package com.github.maxopoly.finale.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.ally.AllyHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

@CommandAlias("ally")
@CommandPermission("finale.ally")
public class AllyCommand extends BaseCommand {

	@Subcommand("add")
	@Description("Mark your friend as an ally.")
	public void ally(Player sender, String targetName) {
		AllyHandler allyHandler = Finale.getPlugin().getManager().getAllyHandler();
		if (!allyHandler.isEnabled()) {
			sender.sendMessage(ChatColor.RED + "Finale Allies are not enabled.");
			return;
		}

		Player target = Bukkit.getPlayer(targetName);
		if (target == null || !target.isOnline()) {
			sender.sendMessage(ChatColor.RED + targetName + " is not online.");
			return;
		}

		if (target.getUniqueId().equals(sender.getUniqueId())) {
			sender.sendMessage(ChatColor.RED + "You can't ally yourself.");
			return;
		}

		allyHandler.addAlly(sender, target);

		target.sendMessage(sender.getName() + " has marked you as an ally.");
		sender.sendMessage(target.getName() + " is marked as your ally.");
	}

	@Subcommand("remove")
	@Description("Mark your friend as an ally.")
	public void unally(Player sender, String targetName) {
		AllyHandler allyHandler = Finale.getPlugin().getManager().getAllyHandler();
		if (!allyHandler.isEnabled()) {
			sender.sendMessage(ChatColor.RED + "Finale Allies are not enabled.");
			return;
		}

		Player target = Bukkit.getPlayer(targetName);
		if (target == null || !target.isOnline()) {
			sender.sendMessage(ChatColor.RED + targetName + " is not online.");
			return;
		}

		/*if (target.getUniqueId().equals(sender.getUniqueId())) {
			sender.sendMessage(ChatColor.RED + "You can't unally yourself.");
			return;
		}*/

		allyHandler.removeAlly(sender, target);

		target.sendMessage(sender.getName() + " has unmarked you as an ally.");
		sender.sendMessage(target.getName() + " is no longer marked as your ally.");
	}

	@Subcommand("list")
	@Description("List your allies")
	public void list(Player sender) {
		AllyHandler allyHandler = Finale.getPlugin().getManager().getAllyHandler();
		if (!allyHandler.isEnabled()) {
			sender.sendMessage(ChatColor.RED + "Finale Allies are not enabled.");
			return;
		}

		Set<UUID> allies = allyHandler.getAllies(sender);
		if (allies.isEmpty()) {
			sender.sendMessage("No allies");
			return;
		}
		for (UUID uuid : allies) {
			Player ally = Bukkit.getPlayer(uuid);
			sender.sendMessage(ally.getName());
		}
	}

}
