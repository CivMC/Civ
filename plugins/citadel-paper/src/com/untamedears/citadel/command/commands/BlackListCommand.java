package com.untamedears.citadel.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.Faction;
import static com.untamedears.citadel.Utility.sendMessage;

public class BlackListCommand extends PlayerCommand{

	public BlackListCommand() {
		super("Black List");
		setDescription("Black Lists a player from your group.");
		setUsage("/ctblacklist §8<group-name> <player name>");
		setArgumentRange(2,2);
		setIdentifiers(new String[] {"ctblacklist"});
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		System.out.println("Black went");
		GroupManager groupManager = Citadel.getGroupManager();
		String groupName = args[0];
		Faction group = groupManager.getGroup(groupName);
		if (args.length < 2){
			sendMessage(sender, ChatColor.RED, "Not enough args.");
			return true;
		}
		if (args.length > 2){
			sendMessage(sender, ChatColor.RED, "Too many args.");
			return true;
		}
		if (group == null){
			sendMessage(sender, ChatColor.RED, "Group doesn't exist.");
			return true;
		}
		if (group.isDisciplined()) {
			sendMessage(sender, ChatColor.RED, Faction.kDisciplineMsg);
			return true;
		}
		if (group.isPersonalGroup() || group == null){
			sendMessage(sender, ChatColor.RED, "Can't BlackList personal groups.");
			return true;
		}
		String senderName = sender.getName();
		if(!group.isFounder(senderName) && !group.isModerator(senderName)){
			sendMessage(sender, ChatColor.RED, "Invalid permission to access this group.");
			return true;
		}
		if (groupManager.addPlayertoBlackList(group, args[1])){
			sendMessage(sender, ChatColor.RED, "Player " + args[1] + " has been added to black list.");
		}
		else{
			sendMessage(sender, ChatColor.RED, "Player " + args[1] + " has been removed from black list.");
		}
		
		return true;
	}

}
