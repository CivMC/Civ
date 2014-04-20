package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

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
public class JoinCommand extends PlayerCommand {

	public JoinCommand() {
		super("Join Group");
        setDescription("Joins a group");
        setUsage("/ctjoin §8<group-name> <password>");
        setArgumentRange(2,2);
		setIdentifiers(new String[] {"ctjoin", "ctj"});
	}

	public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
			sender.sendMessage("Player only command");
			return true;
        }
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
        Player player = (Player)sender;
        UUID accountId = player.getUniqueId();
		if(group.isFounder(accountId)){
			sendMessage(sender, ChatColor.RED, "You are already owner of the group %s", groupName);
			return true;
		}
		if(group.isMember(accountId)){
			sendMessage(sender, ChatColor.RED, "You are already a member of the group %s", groupName);
			return true;
		}
		if(group.isModerator(accountId)){
			sendMessage(sender, ChatColor.RED, "You are already a moderator of the group %s", groupName);
		}
		if(group.getPassword() == null
				|| group.getPassword().isEmpty() 
				|| group.getPassword().equalsIgnoreCase("") 
				|| group.getPassword().equalsIgnoreCase("NULL")){
			sendMessage(sender, ChatColor.RED, "Group is not joinable");
			return true;
		}
		String password = args[1];
		if(!group.getPassword().equalsIgnoreCase(password)){
			sendMessage(sender, ChatColor.RED, "Incorrect password");
			return true;
		}
		groupManager.addMemberToGroup(groupName, accountId, player);
		sendMessage(sender, ChatColor.GREEN, "You have joined %s", groupName);
        Player founder = group.getFounderPlayer();
		if(founder != null){
			sendMessage(founder, ChatColor.YELLOW, "%s has joined %s", player.getDisplayName(), groupName);
		}
		return true;
	}

}
