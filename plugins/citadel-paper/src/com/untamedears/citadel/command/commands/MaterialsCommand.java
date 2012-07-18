package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.ReinforcementMaterial;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/21/12
 * Time: 11:21 AM
 */
public class MaterialsCommand extends PlayerCommand {

    public MaterialsCommand() {
        super("List Materials");
        setDescription("List the possible reinforcement materials, their strengths, and requirements");
        setUsage("/ctmaterials");
		setIdentifiers(new String[] {"ctmaterials", "ctmat"});
    }

	public boolean execute(CommandSender sender, String[] args) {
		if (ReinforcementMaterial.VALID.isEmpty()) {
	        sendMessage(sender, ChatColor.YELLOW, "No reinforcement materials available.");
	    } else {
	        List<ReinforcementMaterial> materials = new ArrayList<ReinforcementMaterial>(ReinforcementMaterial.VALID.values());
	        Collections.sort(materials);
	        sendMessage(sender, ChatColor.GREEN, "Reinforcement materials:");
	        for (ReinforcementMaterial m : materials)
	            sendMessage(sender, ChatColor.GREEN, "%s has strength %d and requires %d units.", m.getMaterial().name(), m.getStrength(), m.getRequirements());
	    }
        return true;
	}
}
