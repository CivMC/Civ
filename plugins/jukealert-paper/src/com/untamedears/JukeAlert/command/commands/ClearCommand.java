package com.untamedears.JukeAlert.command.commands;

import org.bukkit.command.CommandSender;
import com.untamedears.JukeAlert.command.PlayerCommand;

public class ClearCommand extends PlayerCommand {

	public ClearCommand() {
		super("Clear");
		setDescription("Clears snitch logs");
		setUsage("/jaclear");
		setArgumentRange(0,0);
		setIdentifier("jaclear");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		//TODO ClearCommand
		return true;
	}

	
	

}
