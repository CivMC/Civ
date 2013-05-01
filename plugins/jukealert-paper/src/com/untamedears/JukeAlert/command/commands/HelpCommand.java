package com.untamedears.JukeAlert.command.commands;

import org.bukkit.command.CommandSender;

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
		//TODO HelpCommand
		return true;
	}

}
