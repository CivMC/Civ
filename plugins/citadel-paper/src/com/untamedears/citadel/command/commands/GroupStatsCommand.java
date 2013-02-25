package com.untamedears.citadel.command.commands;

import org.bukkit.command.CommandSender;

import com.untamedears.citadel.command.CommandUtils;
import com.untamedears.citadel.command.PlayerCommand;

public class GroupStatsCommand extends PlayerCommand {
	
	public GroupStatsCommand() {
		super("View Group Stats");
		setDescription("View citadel group stats");
		setUsage("/ctgstats <group-name>");
		setArgumentRange(1, 1);
		setIdentifiers(new String[] {"ctgstats", "ctgst"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		CommandUtils.printGroupMembers(sender, args[0]);
		CommandUtils.printReinforcements(sender, args[0], CommandUtils.countReinforcements(args[0]));
		return true;
	}
}
