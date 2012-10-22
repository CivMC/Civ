package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.getTruncatedMaterialMessage;
import static com.untamedears.citadel.Utility.sendMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.PlayerReinforcement;

/**
 * User: JonnyD
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class SecurableCommand extends PlayerCommand {

	public SecurableCommand() {
		super("List Securable");
		setDescription("Other than containers and doors, lists the blocks which are securable");
		setUsage("/ctsecurable");
		setIdentifiers(new String[] {"ctsecurable", "cts"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		if (PlayerReinforcement.SECURABLE.isEmpty()) {
            sendMessage(sender, ChatColor.YELLOW, "No other blocks are securable.");
        } else {
            sendMessage(sender, ChatColor.GREEN, getTruncatedMaterialMessage("Securable blocks: ", PlayerReinforcement.SECURABLE));
        }
		return true;
	}
	
}
