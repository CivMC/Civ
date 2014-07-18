package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;
import static com.untamedears.citadel.Utility.toAccountId;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
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
        if (!(sender instanceof Player)) {
			sender.sendMessage("Console curently isn't supported");
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
        final Player player = (Player)sender;
        final UUID playerAccountId = player.getUniqueId();
        if(!group.isFounder(playerAccountId) && !group.isModerator(playerAccountId)){
        	sendMessage(sender, ChatColor.RED, "Invalid access to modify this group");
        	return true;
        }
		if(group.isPersonalGroup()){
			sendMessage(sender, ChatColor.RED, "You cannot allow players to your default group");
			return true;
		}
        UUID targetId = toAccountId(targetName);
        if (targetId == null) {
			sendMessage(sender, ChatColor.RED, "Unknown player: " + targetName);
			return true;
        }
		if(group.isFounder(targetId)){
			if(playerAccountId.equals(targetId)){
				sendMessage(sender, ChatColor.RED, "You are already owner of this group");
			} else {
				sendMessage(sender, ChatColor.RED, "%s already owns this group", targetName);
			}
			return true;
		}
		if(group.isModerator(targetId)){
			sendMessage(sender, ChatColor.RED, "%s is already a moderator of %s", targetName, group.getName());
			return true;
		}
        if(group.isMember(targetId)){
        	sendMessage(sender, ChatColor.RED, "%s is already a member of %s", targetName, group.getName());
        	return true;
        }
        groupManager.addMemberToGroup(groupName, targetId, player);
        sendMessage(sender, ChatColor.GREEN, "Allowed %s access to %s blocks", targetName, groupName);
        final Player onlineTarget = Bukkit.getPlayer(targetId);
        if(onlineTarget != null){
        	sendMessage(onlineTarget, ChatColor.GREEN, 
        			"You have been added to the group %s by %s", group.getName(), sender.getName());
        }
		return true;
	}

}
