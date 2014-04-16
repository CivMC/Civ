package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
import com.untamedears.citadel.MemberManager;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.Faction;

/**
 * User: JonnyD
 * Date: 07/18/12
 * Time: 11:57 PM
 */
public class AllowCommand extends PlayerCommand {

	public AllowCommand() {
		super("Allow Player");
		setDescription("Adds a player to your group");
		setUsage("/ctallow §8<group-name> <player name>");
		setArgumentRange(2,2);
		setIdentifiers(new String[] {"ctallow", "cta"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		if(!(sender instanceof Player)){
			return true;
		}
		String groupName = args[0];
        String targetName = args[1];
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
		if(group.isFounder(targetName)){
			if(group.isFounder(senderName)){
				sendMessage(sender, ChatColor.RED, "You are already owner of this group");
			} else {
				sendMessage(sender, ChatColor.RED, "%s already owns this group", targetName);
			}
			return true;
		}
		if(group.isModerator(targetName)){
			sendMessage(sender, ChatColor.RED, "%s is already a moderator of %s", targetName, group.getName());
			return true;
		}
        if(group.isMember(targetName)){
        	sendMessage(sender, ChatColor.RED, "%s is already a member of %s", targetName, group.getName());
        	return true;
        }
        Player player = null;
        if (sender instanceof Player) {
            player = (Player)sender;
        }
        MemberManager memberManager = Citadel.getMemberManager();
        groupManager.addMemberToGroup(groupName, targetName, player);
        sendMessage(sender, ChatColor.GREEN, "Allowed %s access to %s blocks", targetName, groupName);
        if(memberManager.isOnline(targetName)){
        	sendMessage(memberManager.getOnlinePlayer(targetName), ChatColor.GREEN, 
        			"You have been added to the group %s by %s", group.getName(), sender.getName());
        }
		return true;
	}

}
