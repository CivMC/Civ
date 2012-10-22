package com.untamedears.citadel.command.commands;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.ReinforcementManager;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;

public class GroupStatsCommand extends PlayerCommand {
	
	public GroupStatsCommand() {
		super("View Group Stats");
		setDescription("View citadel group stats");
		setUsage("/ctgstats <group-name>");
		setIdentifiers(new String[] {"ctgstats"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		sender.sendMessage("ctgstats");
		if (sender instanceof Player || args == null || args.length != 1) {
			return false;
		}
		printReinforcements(sender, args[0], countReinforcements(args[0]));
		
		return true;
	}
	
	protected HashMap<Material,Integer> countReinforcements(String name) {
		HashMap<Material,Integer> hash = new HashMap<Material,Integer>();
		ReinforcementManager reinforcementManager = Citadel.getReinforcementManager();
		Set<IReinforcement> set = reinforcementManager.getReinforcementsByGroup(name);
		Material mat;
		for (IReinforcement r : set) {
			PlayerReinforcement pr = (PlayerReinforcement)r;
			mat = pr.getMaterial().getMaterial();
			if (hash.containsKey(mat)) {
				hash.put(mat, hash.get(mat)+1);
			} else {
				hash.put(mat, 1);
			}
		}
		
		return hash;
	}
	
	protected void printReinforcements(CommandSender sender, String name, HashMap<Material, Integer> reinforcements) {
		sender.sendMessage("Group name: "+name);
		Set<Material> mats = reinforcements.keySet();
		for (Material m : mats) {
			sender.sendMessage(m.name()+": "+reinforcements.get(m));
		}
	}

}
