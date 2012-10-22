package com.untamedears.citadel.command.commands;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.ReinforcementManager;
import com.untamedears.citadel.command.CommandUtils;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;

public class GroupStatsCommand extends PlayerCommand {
	
	public GroupStatsCommand() {
		super("View Group Stats");
		setDescription("View citadel group stats");
		setUsage("/ctgstats <group-name>");
		setArgumentRange(1, 1);
		setIdentifiers(new String[] {"ctgstats", "ctgst"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			sender.sendMessage(ChatColor.RED+"You do not have permission to use this command.");
			return false;
		}
		CommandUtils.printGroupMembers(sender, args[0]);
		CommandUtils.printReinforcements(sender, args[0], CommandUtils.countReinforcements(args[0]));
		
		return true;
	}
}
