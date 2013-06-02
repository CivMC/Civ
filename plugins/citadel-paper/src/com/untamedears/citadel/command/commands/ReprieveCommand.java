package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.Faction;

public class ReprieveCommand extends PlayerCommand {

	public ReprieveCommand() {
		super("Reprieve Group");
		setDescription("Reprieves a group");
		setUsage("/ctreprieve §8 <group-name>");
		setArgumentRange(1,1);
		setIdentifiers(new String[] {"ctarg"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		String group_name = args[0];
		GroupManager group_manager = Citadel.getGroupManager();
		Faction group = group_manager.getGroup(group_name);
		if(group == null){
			sendMessage(sender, ChatColor.RED, "Group doesn't exist");
			return true;
		}
        if (group.isDeleted()) {
			sendMessage(sender, ChatColor.RED, "Group is deleted, sorry");
            return true;
        }
        if (!group.isDisabled()) {
			sendMessage(sender, ChatColor.YELLOW, "Group is not disabled");
            return true;
        }
        Player player = null;
        if (sender instanceof Player) {
            player = (Player)sender;
        }
		group.setDisabled(false);
		sendMessage(sender, ChatColor.GREEN, "Group %s is enabled", group_name);
        group_manager.addGroup(group, player);
		return true;
	}
}
