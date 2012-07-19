package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.Faction;

/**
 * User: JonnyD
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class CreateCommand extends PlayerCommand {

	public CreateCommand(){
		super("Create Group");
		setDescription("Creates a new group");
		setUsage("/ctcreate §8<group-name>");
		setArgumentRange(1, 1);
		setIdentifiers(new String[] {"ctcreate", "ctc"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		GroupManager groupManager = Citadel.getGroupManager();
		String groupName = args[0];
		String senderName = sender.getName();
		if(groupManager.isGroup(groupName)){
			Faction group = groupManager.getGroup(groupName);
			if(group.isFounder(senderName)){
				sendMessage(sender, ChatColor.RED, "You already own this group");
			} else {
				sendMessage(sender, ChatColor.RED, "Group name already taken. Try another");
			}
			return true;
		}
		if(groupManager.getPlayerGroupsAmount(senderName) >= 60){
			sendMessage(sender, ChatColor.RED, "You already have too many groups. 60 is the limit. Try deleting one first");
			return true;
		}
		Faction group = new Faction(groupName, senderName);
		groupManager.addGroup(group);
		sendMessage(sender, ChatColor.GREEN, "Created group: %s", groupName);
		return true;
	}
}
