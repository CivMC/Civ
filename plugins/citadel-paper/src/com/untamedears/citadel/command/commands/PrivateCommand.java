package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.setSingleMode;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.PlayerState;

/**
 * User: JonnyD
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class PrivateCommand extends PlayerCommand {

	public PrivateCommand() {
		super("Private Mode");
		setDescription("Toggle private mode");
		setUsage("/ctprivate");
		setIdentifiers(new String[] {"ctprivate", "ctpr"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		PlayerState state = PlayerState.get(player);
		state.setFaction(Faction.getPersonalGroup(player.getUniqueId()));
		setSingleMode(SecurityLevel.PRIVATE, state, player);
		return true;
	}

}
