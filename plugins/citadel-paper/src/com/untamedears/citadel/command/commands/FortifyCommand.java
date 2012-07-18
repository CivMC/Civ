package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.getSecurityLevel;
import static com.untamedears.citadel.Utility.sendMessage;
import static com.untamedears.citadel.Utility.setMultiMode;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.PlacementMode;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.PlayerState;
import com.untamedears.citadel.entity.ReinforcementMaterial;

/**
 * User: JonnyD
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class FortifyCommand extends PlayerCommand {

	public FortifyCommand() {
		super("Fority Mode");
		setDescription("Toggle fortification mode");
		setUsage("/ctfortify §8<security-level>");
		setArgumentRange(0, 1);
		setIdentifiers(new String[] {"ctfortify", "ctf"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		PlayerState state = PlayerState.get(player);
		
		SecurityLevel securityLevel = getSecurityLevel(args, player);
        if (securityLevel == null) return false;

        ReinforcementMaterial material = ReinforcementMaterial.get(player.getItemInHand().getType());
        if (material == null) {
            sendMessage(sender, ChatColor.YELLOW, "Invalid reinforcement material %s", player.getItemInHand().getType().name());
        } else {
            state.setFortificationMaterial(material);
            setMultiMode(PlacementMode.FORTIFICATION, securityLevel, args, player, state);
        }
        
        return true;
	}

}
