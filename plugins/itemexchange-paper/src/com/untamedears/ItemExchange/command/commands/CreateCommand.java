package com.untamedears.ItemExchange.command.commands;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.ItemExchange.command.PlayerCommand;
import com.untamedears.ItemExchange.utility.InteractionResponse;
import com.untamedears.ItemExchange.utility.InteractionResponse.InteractionResult;
import com.untamedears.ItemExchange.utility.ExchangeRule;
import org.bukkit.Location;

public class CreateCommand extends PlayerCommand {
	public CreateCommand() {
		super("Create Exchange");
		setDescription("Automatically creates an exchange inside the chest the player is looking at");
		setUsage("/iecreate");
		setArgumentRange(0, 1);
		setIdentifiers(new String[] {"iecreate", "iec"});
	}		

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if(args.length==0){
			Location location=((Player)sender).getLastTwoTargetBlocks(null,20).get(1).getLocation();
			InteractionResponse.messagePlayerResult((Player) sender,ItemExchangePlugin.createExchange(location,(Player)sender));
		}
		else if(args.length==1)
		{
			ExchangeRule.RuleType ruleType=null;
			if(args[0].equalsIgnoreCase("input")){
				ruleType=ExchangeRule.RuleType.INPUT;
			}
			else if(args[0].equalsIgnoreCase("output")){
				ruleType=ExchangeRule.RuleType.INPUT;
			}
			if(ruleType!=null){
				if(ItemExchangePlugin.ACCEPTABLE_BLOCKS.contains(((Player)sender).getLastTwoTargetBlocks(null,20).get(1).getLocation().getBlock().getState().getType()))
				{
					InteractionResponse.messagePlayerResult((Player) sender,ItemExchangePlugin.createExchange(((Player)sender).getLastTwoTargetBlocks(null,20).get(1).getLocation(),(Player)sender));
				}
				else
				{
					InteractionResponse.messagePlayerResult((Player) sender,ItemExchangePlugin.createRuleBlock((Player)sender,ruleType));
				}
			}
			else{
			InteractionResponse.messagePlayerResult((Player) sender,new InteractionResponse(InteractionResult.SUCCESS,"Please specify input or output."));
			}
		}
		return true;
	}
	
}
