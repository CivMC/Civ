package com.untamedears.JukeAlert.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.fusesource.jansi.Ansi.Color;

import com.untamedears.JukeAlert.JukeAlert;

public class CommandHandler implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (label.equalsIgnoreCase("jahelp")) {
			player.sendMessage(Color.RED + "Help \n ");
			return true;
		}
		return false;
	}
}
