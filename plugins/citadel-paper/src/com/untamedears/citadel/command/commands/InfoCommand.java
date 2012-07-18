package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.getSecurityLevel;
import static com.untamedears.citadel.Utility.setMultiMode;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.PlacementMode;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.PlayerState;

/**
 * User: JonnyD & chrisrico
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class InfoCommand extends PlayerCommand {

	public InfoCommand() {
		super("Info Mode");
		setDescription("Toggle info mode");
		setUsage("/ctinfo");
		setIdentifiers(new String[] {"ctinfo", "cti"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		PlayerState state = PlayerState.get(player);
		
		SecurityLevel securityLevel = getSecurityLevel(args, player);
        if (securityLevel == null) return false;
        
        setMultiMode(PlacementMode.INFO, SecurityLevel.PUBLIC, args, player, state);
		return true;
	}

}
