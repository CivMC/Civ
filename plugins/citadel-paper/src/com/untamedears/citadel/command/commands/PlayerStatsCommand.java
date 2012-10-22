package com.untamedears.citadel.command.commands;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
import com.untamedears.citadel.ReinforcementManager;
import com.untamedears.citadel.command.CommandUtils;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.Faction;

public class PlayerStatsCommand extends PlayerCommand {
	
	public PlayerStatsCommand() {
		super("View Player Stats");
		setDescription("View citadel player stats");
		setUsage("/ctgstats <player-name>");
		setArgumentRange(1, 1);
		setIdentifiers(new String[] {"ctpstats", "ctpst"});
	}
	
	public boolean execute(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			sender.sendMessage(ChatColor.RED+"You do not have permission to use this command.");
			return false;
		}
		
		GroupManager groupManager = Citadel.getGroupManager();
		Set<Faction> memberGroups = groupManager.getGroupsByMember(args[0]);
		Set<Faction> moderatorGroups = groupManager.getGroupsByModerator(args[0]);
		Set<Faction> founderGroups = groupManager.getGroupsByFounder(args[0]);
		sender.sendMessage("Player name: "+args[0]);
		if (founderGroups.size() > 0)
			sender.sendMessage("Admin of groups: "+CommandUtils.joinFactionSet(founderGroups));
		if (moderatorGroups.size() > 0)
			sender.sendMessage("Moderator of groups: "+CommandUtils.joinFactionSet(moderatorGroups));
		if (memberGroups.size() > 0)
			sender.sendMessage("Member of groups: "+CommandUtils.joinFactionSet(memberGroups));
		
		Faction group = Citadel.getMemberManager().getMember(args[0]).getPersonalGroup();
		if (group != null) {
			String personalGroupName = group.getName();
			sender.sendMessage("Personal group reinforcements: ");
			CommandUtils.printReinforcements(sender, args[0], CommandUtils.countReinforcements(personalGroupName));
		} else {
			sender.sendMessage("Player has no personal group.");
		}
		
		return false;
	}

}
