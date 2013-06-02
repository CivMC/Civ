package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.Faction;

public class DisciplineCommand extends PlayerCommand {

	public DisciplineCommand() {
		super("Discipline Group");
		setDescription("Disciplines a group");
		setUsage("/ctdiscipline §8 [del] <group-name>");
		setArgumentRange(1,2);
		setIdentifiers(new String[] {"ctadg"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		String group_name = null;
		boolean delete_group = false;
		if (args[0].equalsIgnoreCase("del")) {
			delete_group = true;
			group_name = args[1];
		} else {
			group_name = args[0];
		}
		GroupManager group_manager = Citadel.getGroupManager();
		Faction group = group_manager.getGroup(group_name);
		if (group == null) {
			sendMessage(sender, ChatColor.RED, "Group doesn't exist");
			return true;
		}
        if (group.isDeleted()) {
			sendMessage(sender, ChatColor.YELLOW, "Group already deleted");
			return true;
        }
        if (group.isDisabled()) {
            if (delete_group) {
			    group.setDisabled(false);
            } else {
			    sendMessage(sender, ChatColor.YELLOW, "Group already disabled");
			    return true;
            }
        }
		if (delete_group) {
			group.setDeleted(true);
			sendMessage(sender, ChatColor.GREEN, "Group %s is deleted", group_name);
		} else {
			group.setDisabled(true);
			sendMessage(sender, ChatColor.GREEN, "Group %s is disabled", group_name);
		}
        Player player = null;
        if (sender instanceof Player) {
            player = (Player)sender;
        }
        group_manager.addGroup(group, player);
		return true;
	}
}
