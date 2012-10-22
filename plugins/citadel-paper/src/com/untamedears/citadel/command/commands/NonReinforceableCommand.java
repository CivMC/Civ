package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.getTruncatedMaterialMessage;
import static com.untamedears.citadel.Utility.sendMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.PlayerReinforcement;

public class NonReinforceableCommand extends PlayerCommand {

	public NonReinforceableCommand() {
		super("List Non Reinforceable");
		setDescription("Lists the blocks which may not be reinforced");
		setUsage("/ctnonreinforceable");
		setIdentifiers(new String[] {"ctnonreinforceable", "ctn"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		if (PlayerReinforcement.NON_REINFORCEABLE.isEmpty()) {
             sendMessage(sender, ChatColor.YELLOW, "No blocks are non-reinforceable.");
         } else {
             sendMessage(
                     sender, ChatColor.GREEN,
                     getTruncatedMaterialMessage("Non-reinforceable blocks: ", PlayerReinforcement.NON_REINFORCEABLE));
         }
		 return true;
	}
	
}
