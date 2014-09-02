package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

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
public class PasswordCommand extends PlayerCommand {

	public PasswordCommand() {
		super("Set Group Password");
        setDescription("Sets the password for a group. Set password to \"null\" to make your group not joinable");
        setUsage("/ctpassword §8<group-name> <password>");
        setArgumentRange(2,2);
		setIdentifiers(new String[] {"ctpassword", "ctpw"});
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
		if(!group.isFounder(player.getUniqueId())){
			sendMessage(sender, ChatColor.RED, "Invalid permission to modify this group");
			return true;
		}
		String password = args[1];
		if(password.isEmpty() || password.equals("")){
			sendMessage(sender, ChatColor.RED, "Please enter a password");
			return true;
		}
		group.setPassword(password);
		groupManager.addGroup(group, player);
		sendMessage(sender, ChatColor.GREEN, "Changed password for %s to \"%s\"", groupName, password);
		return true;
	}

}
