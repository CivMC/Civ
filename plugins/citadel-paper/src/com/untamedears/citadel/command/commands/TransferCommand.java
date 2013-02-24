package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
import com.untamedears.citadel.MemberManager;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.Member;

/**
 * User: JonnyD
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class TransferCommand extends PlayerCommand {

	public TransferCommand() {
		super("Transfer Group");
		setDescription("Transfers a group to another player. WARNING: You lose reinforcements associated with this group. This cannot be undone.");
		setUsage("/cttransfer §8<group-name> <player-name>");
		setArgumentRange(2, 2);
		setIdentifiers(new String[] {"cttransfer", "ctt"});
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
			sendMessage(sender, ChatColor.RED, "Invalid permission to transfer this group");
			return true;
		}
		if(group.isPersonalGroup()){
			sendMessage(sender, ChatColor.RED, "You cannot transfer your default group");
			return true;
		}
		String targetName = args[1];
		if(senderName.equalsIgnoreCase(targetName)){
			sendMessage(sender, ChatColor.RED, "You already own this group");
			return true;
		}
		int groupsAllowed = Citadel.getConfigManager().getGroupsAllowed();
		if(groupManager.getPlayerGroupsAmount(targetName) >= groupsAllowed){
			sendMessage(sender, ChatColor.RED, "This player has already reached the maximum amount of groups allowed");
			return true;
		}
		MemberManager memberManager = Citadel.getMemberManager();
		if(!memberManager.isOnline(targetName)){
			sendMessage(sender, ChatColor.RED, "User must be online");
			return true;
		}
		Member member = memberManager.getMember(targetName);
		if(member == null){
			member = new Member(targetName);
			memberManager.addMember(member);
		}
		if(group.isMember(targetName)){
			groupManager.removeMemberFromGroup(groupName, targetName);
		}
		if(group.isModerator(targetName)){
			groupManager.removeModeratorFromGroup(groupName, targetName);
		}
		group.setFounder(targetName);
		groupManager.addGroup(group);
		sendMessage(sender, ChatColor.GREEN, "You have transferred %s to %s", groupName, targetName);
		if(memberManager.isOnline(targetName)){
			sendMessage(memberManager.getOnlinePlayer(targetName), ChatColor.YELLOW, "%s has transferred the group %s to you", 
					senderName, groupName);
		}
		return true;
	}

}
