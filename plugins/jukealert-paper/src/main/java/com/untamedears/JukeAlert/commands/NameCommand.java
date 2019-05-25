package com.untamedears.JukeAlert.commands;

import static com.untamedears.JukeAlert.util.Utility.findLookingAtOrClosestSnitch;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.util.JukeAlertPermissionHandler;
import com.untamedears.JukeAlert.util.Utility;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "janame")
public class NameCommand extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;

		String name = "";
		if (args[0].length() > 40) {
			name = args[0].substring(0, 40);
		} else {
			name = args[0];
		}
		Snitch snitch = findLookingAtOrClosestSnitch(player, JukeAlertPermissionHandler.getRenameSnitch());
		if (snitch == null) {
			player.sendMessage(
					ChatColor.RED + "You do not own any snitches nearby or lack permission to view their logs!");
			return true;
		}
		String prevName = snitch.getName();
		snitch.setName(name);
		TextComponent lineText = new TextComponent(ChatColor.AQUA + " Changed snitch name to ");
		lineText.addExtra(Utility.genTextComponent(snitch));
		lineText.addExtra(ChatColor.AQUA + " from " + ChatColor.GOLD + prevName);
		player.spigot().sendMessage(lineText);
		return true;

	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new LinkedList<>();
	}
}
