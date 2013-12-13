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
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class LeaveCommand extends PlayerCommand {

	public LeaveCommand() {
		super("Leave");
        setDescription("Leave a group");
        setUsage("/ctleave §8<group-name>");
        setArgumentRange(1,1);
		setIdentifiers(new String[] {"ctleave", "ctl"});
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
		String playerName = sender.getName();
		if(group.isFounder(playerName)){
			sendMessage(sender, ChatColor.RED, "You are the owner. If you wish to leave you must either delete or transfer the group");
			return true;
		}
		if(group.isPersonalGroup()){
			sendMessage(sender, ChatColor.RED, "You cannot leave your default group");
			return true;
		}
		if(!group.isMember(playerName) && !group.isModerator(playerName)){
			sendMessage(sender, ChatColor.RED, "You are not a member of %s", group.getName());
			return true;
		}
        Player player = null;
        if (sender instanceof Player) {
            player = (Player)sender;
        }
		if(group.isModerator(playerName)){
			groupManager.removeModeratorFromGroup(groupName, playerName, player);
		}
		if(group.isMember(playerName)){
			groupManager.removeMemberFromGroup(groupName, playerName, player);
		}
		sendMessage(sender, ChatColor.GREEN, "You have left the group %s", group.getName());
		MemberManager memberManager = Citadel.getMemberManager();
		if(memberManager.isOnline(group.getFounder())){
			Player founder = memberManager.getOnlinePlayer(group.getFounder());
			sendMessage(founder, ChatColor.YELLOW, "%s has left the group %s", playerName, group.getName());
		}
		return true;
	}

}
