package com.untamedears.citadel.command;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
import com.untamedears.citadel.ReinforcementManager;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.FactionMember;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.Moderator;
import com.untamedears.citadel.entity.PlayerReinforcement;

public final class CommandUtils {
	public static HashMap<Material,Integer> countReinforcements(String name) {
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
	
	public static void formatReinforcements(List<String> output, String name, HashMap<Material, Integer> reinforcements) {
		output.add("Group name: "+name);
		Set<Material> mats = reinforcements.keySet();
		for (Material m : mats) {
			output.add(m.name()+": "+reinforcements.get(m));
		}
	}
	
	protected static String joinModeratorSet(Set<Moderator> set) {
		String result = "";
		int size = set.size();
		int i = 0;
		for (Moderator m : set) {
			i++;
			result+=m.getPlayerName();
			if (i < size) {
				result+= ", ";
			}
		}
		return result;
	}
	
	protected static String joinMemberSet(Set<FactionMember> set) {
		String result = "";
		int size = set.size();
		int i = 0;
		for (FactionMember m : set) {
			i++;
			result+=m.getPlayerName();
			if (i < size) {
				result+= ", ";
			}
		}
		return result;
	}
	
	public static String joinFactionSet(Set<Faction> set) {
		String result = "";
		int size = set.size();
		int i = 0;
		for (Faction f : set) {
			i++;
			result+=f.getName();
			if (i < size) {
				result+= ", ";
			}
		}
		return result;
	}
	
	public static void formatGroupMembers(List<String> output, String name) {
		GroupManager groupManager = Citadel.getGroupManager();
		Faction group = groupManager.getGroup(name);
		if (group != null) {
			output.add("Group name: "+name);
			output.add("Admin: "+group.getFounderName());
			output.add("Moderators: "+joinModeratorSet(groupManager.getModeratorsOfGroup(name)));
			output.add("Members: "+joinMemberSet(groupManager.getMembersOfGroup(name)));
		}
	}
}
