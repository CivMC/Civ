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
public class AddModCommand extends PlayerCommand {

	public AddModCommand() {
		super("Add Moderator");
		setDescription("Adds a player as moderator to a group");
		setUsage("/ctaddmod §8<group-name> <player-name>");
		setArgumentRange(2,2);
		setIdentifiers(new String[] {"ctaddmod", "ctam"});
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
        Player player = (Player)sender;
		if(!group.isFounder(player.getUniqueId())){
			sendMessage(sender, ChatColor.RED, "Invalid permission to modify this group");
			return true;
		}
		if(group.isPersonalGroup()){
			sendMessage(sender, ChatColor.RED, "You cannot allow moderators to your default group");
			return true;
		}
        UUID targetId = toAccountId(targetName);
        if (targetId == null) {
			sendMessage(sender, ChatColor.RED, "Unknown player: " + targetName);
			return true;
        }
		if(group.isFounder(targetId)){
			sendMessage(sender, ChatColor.RED, "You are already owner of this group");
			return true;
		}
		if(group.isModerator(targetId)){
			sendMessage(sender, ChatColor.RED, "%s is already a moderator of %s", targetName, groupName);
			return true;
		}
		if(group.isMember(targetId)){
			groupManager.removeMemberFromGroup(groupName, targetId, player);
		}
		groupManager.addModeratorToGroup(groupName, targetId, player);
		sendMessage(sender, ChatColor.GREEN, "%s has been added as a moderator to %s", targetName, groupName);
        final Player onlineTarget = Bukkit.getPlayer(targetId);
		if(onlineTarget != null){
			sendMessage(onlineTarget, ChatColor.GREEN, "You have been added as " +
					"moderator to %s by %s", groupName, player.getDisplayName());
		}
		return true;
	}

}
