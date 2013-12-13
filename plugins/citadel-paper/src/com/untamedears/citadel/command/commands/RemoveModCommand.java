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
public class RemoveModCommand extends PlayerCommand {

	public RemoveModCommand() {
		super("Remove Moderator");
		setDescription("Removes a player as moderator from a group");
		setUsage("/ctremovemod §8<group-name> <player-name>");
		setArgumentRange(2,2);
		setIdentifiers(new String[] {"ctremovemod", "ctrm"});
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
		if(!group.isFounder(senderName)){
			sendMessage(sender, ChatColor.RED, "Invalid permission to modify this group");
			return true;
		}
		String targetName = args[1];
		if(!group.isModerator(targetName)){
			sendMessage(sender, ChatColor.RED, "%s is not a moderator of %s", targetName, groupName);
			return true;
		}
        Player player = null;
        if (sender instanceof Player) {
            player = (Player)sender;
        }
		groupManager.removeModeratorFromGroup(groupName, targetName, player);
		if(!group.isMember(targetName)){
			groupManager.addMemberToGroup(groupName, targetName, player);
		}
		sendMessage(sender, ChatColor.GREEN, "%s has been removed as moderator from %s and demoted to a member. Use /ctdisallow to remove as member", targetName, groupName);
		return true;
	}

}
