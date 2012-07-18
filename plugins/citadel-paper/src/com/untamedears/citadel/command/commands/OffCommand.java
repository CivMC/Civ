package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.PlayerState;

/**
 * User: JonnyD & chrisrico
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class OffCommand extends PlayerCommand {

	public OffCommand() {
		super("Off Mode");
		setDescription("Toggles citadel off");
		setUsage("/ctoff");
		setIdentifiers(new String[] {"ctoff", "cto"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		PlayerState state = PlayerState.get(player);
        
        state.reset();
        if (state.isBypassMode()) state.toggleBypassMode();
        sendMessage(player, ChatColor.GREEN, "All Citadel modes set to normal");
        
		return true;
	}

}
