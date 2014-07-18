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

public class GroupInfoCommand extends PlayerCommand {

	public GroupInfoCommand() {
		super("Group Information");
		setDescription("Displays information about a group");
		setUsage("/ctgroupinfo §8<group-name>");
		setArgumentRange(1,1);
		setIdentifiers(new String[] {"ctgroupinfo", "ctgi"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		String groupName = args[0];
		GroupManager groupManager = Citadel.getGroupManager();
		Faction group = groupManager.getGroup(groupName);
		if(group == null){
			sendMessage(sender, ChatColor.RED, "Group doesn't exist");
			return true;
		}
        Player player = (Player)sender;
        UUID accountId = player.getUniqueId();
        boolean founderOrModerator = group.isFounder(accountId) || group.isModerator(accountId);
		if(!founderOrModerator && !group.isMember(accountId)){
			sendMessage(sender, ChatColor.RED, "Invalid permission to access this group");
			return true;
		}
		sender.sendMessage(new StringBuilder().append("§cGroup Name:§e ").append(groupName).toString());
		sender.sendMessage(new StringBuilder().append("§cOwner:§e ").append(group.getFounder()).toString());
		sender.sendMessage(new StringBuilder().append("§cModerators:§e ").append(groupManager.getModeratorsOfGroup(groupName).size()).toString());
		sender.sendMessage(new StringBuilder().append("§cMembers:§e ").append(groupManager.getMembersOfGroup(groupName).size()).toString());
		if(founderOrModerator){
			String password = group.getPassword();
			sender.sendMessage(new StringBuilder().append("§cPassword:§e ").append(password).toString());
			String joinable = "";
			if(password != null && !password.equalsIgnoreCase("null")){
				joinable = "Yes";
			} else {
				joinable = "No";
			}
			sender.sendMessage(new StringBuilder().append("§cJoinable:§e ").append(joinable).toString());
		}
		if (group.isDisciplined()) {
			StringBuilder discipline = new StringBuilder().append("§cDiscipline:§e");
   			if (group.isDisabled()) {
				discipline.append(" Disabled");
			}
			if (group.isDeleted()) {
				discipline.append(" Deleted");
			}
			sender.sendMessage(discipline.toString());
		}
		return true;
	}

}
