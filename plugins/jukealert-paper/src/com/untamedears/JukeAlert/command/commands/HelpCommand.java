package com.untamedears.JukeAlert.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.command.PlayerCommand;

public class HelpCommand extends PlayerCommand {

	public HelpCommand() {
		super("Help");
		setDescription("Displays help");
		setUsage("/jahelp");
		setArgumentRange(0,0);
		setIdentifier("jahelp");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (sender instanceof Player){
			Player player = (Player) sender;
			player.sendMessage(ChatColor.AQUA+ "Commands are: \n jahelp: Gives you possible Commands. \n jainfo: Gives You information in the Snitch. \n jaclear: Clears the snitch. \n janame: Names the Snitch. \n jalist: Gives info for snitches you own. \n jamute: Adds or removes from juke alert ignore list.");
		}
		
		return true;
	}

}
