package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.getSecurityLevel;
import static com.untamedears.citadel.Utility.setMultiMode;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.PlacementMode;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.PlayerState;

/**
 * User: JonnyD
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class ReinforceCommand extends PlayerCommand {

	public ReinforceCommand() {
		super("Reinforce Mode");
		setDescription("Toggles reinforce mode");
		setUsage("/ctreinforce §8<security-level>");
		setArgumentRange(0, 1);
		setIdentifiers(new String[] {"ctreinforce", "ctr"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		PlayerState state = PlayerState.get(player);
		state.setFaction(Citadel.getMemberManager().getMember(player).getPersonalGroup());
		SecurityLevel securityLevel = getSecurityLevel(args, player);
        if (securityLevel == null) return false;
        
        setMultiMode(PlacementMode.REINFORCEMENT, securityLevel, args, player, state);
        return true;
	}

}
