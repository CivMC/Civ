package com.untamedears.JukeAlert.command.commands;

import org.bukkit.command.CommandSender;

import com.untamedears.JukeAlert.command.PlayerCommand;

public class InfoCommand extends PlayerCommand {

	public InfoCommand() {
		super("Info");
		setDescription("Displays information from a Snitch");
		setUsage("/jainfo");
		setArgumentRange(0,0);
		setIdentifier("jainfo");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		//TODO infoCommand
		return true;
	}

}
