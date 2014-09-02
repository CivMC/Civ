package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;
import static com.untamedears.citadel.Utility.setSingleMode;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.PlayerState;

/**
 * User: JonnyD & chrisrico
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class GroupCommand extends PlayerCommand {

	public GroupCommand() {
		super("Group Mode");
		setDescription("Toggle group mode");
		setUsage("/ctgroup §8<group-name>");
		setArgumentRange(1,1);
		setIdentifiers(new String[] {"ctgroup", "ctg"});
	}

	public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
			sender.sendMessage("Player only command");
			return true;
        }
		String groupName = args[0];
		Faction group = Citadel.getGroupManager().getGroup(groupName);
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
			sendMessage(sender, ChatColor.RED, "Invalid permission to use this group");
			return true;
		}
		if(group.isPersonalGroup()){
			sendMessage(sender, ChatColor.RED, "You cannot share your default group");
			return true;
		}
		PlayerState state = PlayerState.get(player);
		state.setFaction(group);
		setSingleMode(SecurityLevel.GROUP, state, player);
		return true;		
	}

}
