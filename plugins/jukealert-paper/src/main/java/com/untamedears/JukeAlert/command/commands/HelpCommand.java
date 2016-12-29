package com.untamedears.JukeAlert.command.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class HelpCommand extends PlayerCommand {

	public HelpCommand() {
		super("Help");
		setDescription("Displays help");
		setUsage("/jahelp");
		setArguments(0,0);
		setIdentifier("jahelp");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (sender instanceof Player){
			Player player = (Player) sender;
			player.sendMessage(ChatColor.AQUA+ "Commands are: \n jahelp: Gives you possible Commands. \n jainfo: Gives You information in the Snitch. \n jaclear: Clears the snitch. \n janame: Names the Snitch. \n jalist: Gives info for snitches you own. \n jalistlong: Gives extended info for snitches you own. \n jamute: Adds or removes from juke alert ignore list. \n JaToggleLevers: Sets the flag that indicates if jukes should trigger levers.");
		}

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}

}
