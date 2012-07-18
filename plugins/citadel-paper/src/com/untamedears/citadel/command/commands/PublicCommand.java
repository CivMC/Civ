package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.setSingleMode;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.PlayerState;

/**
 * User: JonnyD
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class PublicCommand extends PlayerCommand {

	public PublicCommand() {
		super("Public Mode");
		setDescription("Toggles public mode");
		setUsage("/ctpublic");
		setIdentifiers(new String[] {"ctpublic", "ctpu"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		PlayerState state = PlayerState.get(player);
		setSingleMode(SecurityLevel.PUBLIC, state, player);
		return true;
	}

}
