package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.Faction;

/**
 * User: JonnyD
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class GroupsCommand extends PlayerCommand {

	public GroupsCommand() {
		super("List Groups");
		setDescription("Lists your groups");
		setUsage("/ctgroups <page>");
		setArgumentRange(0,1);
		setIdentifiers(new String[] {"ctgroups", "ctgs"});
	}

	public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
			sender.sendMessage("Console curently isn't supported");
			return true;
        }
        Player player = (Player)sender;
        UUID accountId = player.getUniqueId();

		GroupManager groupManager = Citadel.getGroupManager();
		
		Faction personalGroup = Faction.getPersonalGroup(accountId);
		String personalGroupName = personalGroup.getName();
		
		List<Faction> groups = new ArrayList<Faction>();
		Set<Faction> memberGroups = groupManager.getGroupsByMember(accountId);
		Set<Faction> moderatedGroups = groupManager.getGroupsByModerator(accountId);
		Set<Faction> ownedGroups = groupManager.getGroupsByFounder(accountId);
		
		groups.addAll(ownedGroups);
		groups.addAll(moderatedGroups);
		groups.addAll(memberGroups);
		
		if(groups.isEmpty()){
			sendMessage(sender, ChatColor.GREEN, "You have no groups");
			return true;
		}		
		int page = 0;
		if(args.length != 0){
			try {
				page = Integer.parseInt(args[0]) - 1;
			} catch (NumberFormatException ignored){
				
			}
		}
		
		int numPages = groups.size() / 8;
		if(groups.size() % 8 != 0){
			numPages++;
		}
		if(numPages == 0){
			numPages = 1;
		}
		
		if((page >= numPages) || (page < 0)){
			page = 0;
		}
		sendMessage(sender, ChatColor.AQUA, "-----[ Your Groups <" + (page + 1) + "/" + numPages + "> ]-----");
		int start = page * 8;
		int end = start + 8;
		if(end > groups.size()){
			end = groups.size();
		}
		for(int g = start; g < end; g++){
			Faction group = groups.get(g);
            StringBuilder line = new StringBuilder()
                .append(group.getName());
			if(ownedGroups.contains(group)){
				line.append(" (Owner)");
			} else if(moderatedGroups.contains(group)){
				line.append(" (Moderator)");
			} else if(memberGroups.contains(group)){
				line.append(" (Member)");
			} 
			if(group.getName().equalsIgnoreCase(personalGroupName)){
				line.append(" (Default Group)");
			}
			if (group.isDisciplined()) {
				line.append(" (Disciplined)");
			}
			sendMessage(sender, ChatColor.WHITE, line.toString());
		}
		if(page + 1 < numPages){
			sendMessage(sender, ChatColor.GRAY, "For more type \"/ctgroups [n]\"");
		}
		return true;
	}

}
