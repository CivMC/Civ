package com.untamedears.citadel.command.commands;

import org.bukkit.command.CommandSender;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.ReinforcementManager;
import com.untamedears.citadel.command.PlayerCommand;

public class PlayerStatsCommand extends PlayerCommand {
	
	public PlayerStatsCommand() {
		super("View Player Stats");
		setDescription("View citadel player stats");
		setUsage("/ctgstats <player-name>");
		setIdentifiers(new String[] {"ctpstats"});
	}
	
	public boolean execute(CommandSender sender, String[] args) {
		ReinforcementManager reinforcementManager = Citadel.getReinforcementManager();
		
		// TODO Auto-generated method stub
		return false;
	}

}
