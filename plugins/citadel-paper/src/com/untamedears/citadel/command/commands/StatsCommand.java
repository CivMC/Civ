package com.untamedears.citadel.command.commands;

import org.bukkit.command.CommandSender;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
import com.untamedears.citadel.ReinforcementManager;
import com.untamedears.citadel.command.PlayerCommand;

/**
 * User: JonnyD
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class StatsCommand extends PlayerCommand {

	public StatsCommand() {
		super("View Stats");
		setDescription("View citadel stats");
		setUsage("/ctstats");
		setIdentifiers(new String[] {"ctstats"});
	}

	public boolean execute(CommandSender sender, String[] args) {		
		ReinforcementManager reinforcementManager = Citadel.getReinforcementManager();
		int numReinforcements = reinforcementManager.getReinforcementsAmount();
		
		GroupManager groupManager = Citadel.getGroupManager();
		int numGroups = groupManager.getGroupsAmount();
		
		sender.sendMessage(new StringBuilder().append("§cTotal Reinforcements:§e " ).append(numReinforcements).toString());
		sender.sendMessage(new StringBuilder().append("§cTotal Groups:§e " ).append(numGroups).toString());
		return true;
	}

}
