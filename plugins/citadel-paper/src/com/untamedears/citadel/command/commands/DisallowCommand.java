package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;
import static com.untamedears.citadel.Utility.toAccountId;

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
public class DisallowCommand extends PlayerCommand {

	public DisallowCommand() {
		super("Disallow Player");
		setDescription("Removes a player from a group");
		setUsage("/ctdisallow §8<group-name> <player-name>");
		setArgumentRange(2,2);
		setIdentifiers(new String[] {"ctdisallow", "ctd"});
	}

	public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
			sender.sendMessage("Console curently isn't supported");
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
        if(!group.isFounder(accountId) && !group.isModerator(accountId)){
        	sendMessage(sender, ChatColor.RED, "Invalid access to modify this group");
        	return true;
        }
		if(group.isPersonalGroup()){
			sendMessage(sender, ChatColor.RED, "You cannot allow players to your default group");
			return true;
		}
        String playerName = args[1];
        UUID targetAccountId = toAccountId(playerName);
        if(!group.isMember(targetAccountId)){
        	sendMessage(sender, ChatColor.RED, "%s is not a member of this group", playerName);
        	return true;
        }
        groupManager.removeMemberFromGroup(groupName, targetAccountId, player);
        sendMessage(sender, ChatColor.GREEN, "Disallowed %s from access to %s blocks", playerName, group.getName());
		return true;
	}

}
