package com.untamedears.JukeAlert.command.commands;

import org.bukkit.command.CommandSender;
import com.untamedears.JukeAlert.command.PlayerCommand;

public class ClearAllCommand extends PlayerCommand {
	public ClearAllCommand() {
		super("clearall");
		setDescription("Clears all snitches logs for a group");
		setUsage("/jaclearall");
		setArgumentRange(0,0);
		setIdentifier("jaclearall");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
			//TODO ClearAllCommand
		return true;
	}
}
