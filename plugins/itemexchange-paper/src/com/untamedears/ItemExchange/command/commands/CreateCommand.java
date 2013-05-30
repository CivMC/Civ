package com.untamedears.ItemExchange.command.commands;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.ItemExchange.command.PlayerCommand;
import com.untamedears.ItemExchange.utility.InteractionResponse;

public class CreateCommand extends PlayerCommand {
	public CreateCommand() {
		super("Create Exchange");
		setDescription("Automatically creates an exchange inside the chest the player is looking at");
		setUsage("/iecreate");
		setIdentifiers(new String[] {"iecreate", "iec"});
	}		

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		InteractionResponse response=ItemExchangePlugin.createExchange(((Player)sender).getLastTwoTargetBlocks(null,20).get(1).getLocation(),(Player)sender);
		InteractionResponse.messagePlayerResult((Player) sender,response);
		return true;
	}
	
}
