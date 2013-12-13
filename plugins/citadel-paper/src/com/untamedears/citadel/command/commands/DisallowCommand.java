package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

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
public class DisallowCommand extends PlayerCommand {

	public DisallowCommand() {
		super("Disallow Player");
		setDescription("Removes a player from a group");
		setUsage("/ctdisallow §8<group-name> <player-name>");
		setArgumentRange(2,2);
		setIdentifiers(new String[] {"ctdisallow", "ctd"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		String groupName = args[0];		
		GroupManager groupManager = Citadel.getGroupManager();
		Faction group = groupManager.getGroup(groupName);
		if(group == null){
        	sendMessage(sender, ChatColor.RED, "Group doesn't exist");
        	return true;
        }
		if (group.isDisciplined()) {
			sendMessage(sender, ChatColor.RED, Faction.kDisciplineMsg);
			return true;
		}
		String senderName = sender.getName();
        if(!group.isFounder(senderName) && !group.isModerator(senderName)){
        	sendMessage(sender, ChatColor.RED, "Invalid access to modify this group");
        	return true;
        }
		if(group.isPersonalGroup()){
			sendMessage(sender, ChatColor.RED, "You cannot allow players to your default group");
			return true;
		}
        String playerName = args[1];
        if(!group.isMember(playerName)){
        	sendMessage(sender, ChatColor.RED, "%s is not a member of this group", playerName);
        	return true;
        }
        Player player = null;
        if (sender instanceof Player) {
            player = (Player)sender;
        }
        groupManager.removeMemberFromGroup(groupName, playerName, player);
        sendMessage(sender, ChatColor.GREEN, "Disallowed %s from access to %s blocks", playerName, group.getName());
		return true;
	}

}
