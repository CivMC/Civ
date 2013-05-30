package com.untamedears.ItemExchange.command.commands;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.ItemExchange.command.PlayerCommand;
import com.untamedears.ItemExchange.utility.InteractionResponse;

public class CreateCommand extends PlayerCommand {
	public CreateCommand() {
		super("C Exchange");
		setDescription("Automatically creates an exchange inside the chest the player is looking at");
		setUsage("/iesetup");
		setIdentifiers(new String[] {"iesetup", "ies"});
	}		

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		InteractionResponse response;
		if(ItemExchangePlugin.ACCEPTABLE_BLOCKS.contains(((Player)sender).getLastTwoTargetBlocks(null,20).get(1).getLocation().getBlock().getState().getType()))
		{
			response=ItemExchangePlugin.createExchange(((Player)sender).getLastTwoTargetBlocks(null,20).get(1).getLocation(),(Player)sender);
		}
		else
		{
			response=ItemExchangePlugin.createRuleBlock((Player)sender);
		}
		
		InteractionResponse.messagePlayerResult((Player) sender,response);
		return true;
	}
	
}
