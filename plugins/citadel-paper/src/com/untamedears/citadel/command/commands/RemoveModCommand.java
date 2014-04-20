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
public class RemoveModCommand extends PlayerCommand {

	public RemoveModCommand() {
		super("Remove Moderator");
		setDescription("Removes a player as moderator from a group");
		setUsage("/ctremovemod §8<group-name> <player-name>");
		setArgumentRange(2,2);
		setIdentifiers(new String[] {"ctremovemod", "ctrm"});
	}

	public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
			sender.sendMessage("Console curently isn't supported");
			return true;
        }
		final String groupName = args[0];
		final GroupManager groupManager = Citadel.getGroupManager();
		final Faction group = groupManager.getGroup(groupName);
		if(group == null){
			sendMessage(sender, ChatColor.RED, "Group doesn't exist");
			return true;
		}
		if (group.isDisciplined()) {
			sendMessage(sender, ChatColor.RED, Faction.kDisciplineMsg);
			return true;
		}
        final Player player = (Player)sender;
		if(!group.isFounder(player.getUniqueId())){
			sendMessage(sender, ChatColor.RED, "Invalid permission to modify this group");
			return true;
		}
        final String targetName = args[1];
        final UUID targetId = toAccountId(targetName);
        if (targetId == null) {
			sendMessage(sender, ChatColor.RED, "Unknown player: " + targetName);
			return true;
        }
		if(!group.isModerator(targetId)){
			sendMessage(sender, ChatColor.RED, "%s is not a moderator of %s", targetName, groupName);
			return true;
		}
		groupManager.removeModeratorFromGroup(groupName, targetId, player);
		if(!group.isMember(targetId)){
			groupManager.addMemberToGroup(groupName, targetId, player);
		}
		sendMessage(sender, ChatColor.GREEN, "%s has been removed as moderator from %s and demoted to a member. Use /ctdisallow to remove as member", targetName, groupName);
		return true;
	}

}
