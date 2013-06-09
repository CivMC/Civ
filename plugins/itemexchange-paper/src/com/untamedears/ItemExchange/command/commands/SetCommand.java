package com.untamedears.ItemExchange.command.commands;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.ItemExchange.command.PlayerCommand;
import com.untamedears.ItemExchange.utility.ExchangeRule;
import com.untamedears.ItemExchange.exceptions.ExchangeRuleParseException;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SetCommand extends PlayerCommand {
	public SetCommand() {
		super("Set Field");
		setDescription("Sets the field of the ExchangeRule held in hand");
		setUsage("/ieset");
		setArgumentRange(1, 3);
		setIdentifiers(new String[] {"ieset", "ies"});
	}		

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		try{
			ExchangeRule exchangeRule=ExchangeRule.parseRuleBlock(((Player)sender).getItemInHand());
			if(args[0].equalsIgnoreCase("commonname") || args[0].equalsIgnoreCase("c")) {
				ItemStack itemStack=ItemExchangePlugin.NAME_MATERIAL.get(args[1]);
				exchangeRule.setMaterial(itemStack.getType());
				exchangeRule.setDurability(itemStack.getDurability());
			}
			else if(args[0].equalsIgnoreCase("material") || args[0].equalsIgnoreCase("m")) {
				exchangeRule.setMaterial(Material.getMaterial(args[1]));
			}
			else if(args[0].equalsIgnoreCase("amount") || args[0].equalsIgnoreCase("a")) {
				exchangeRule.setAmount(Integer.valueOf(args[1]));
			}
			else if(args[0].equalsIgnoreCase("durability") || args[0].equalsIgnoreCase("d")) {
				exchangeRule.setDurability(Short.valueOf(args[1]));
			}
			else if(args[0].equalsIgnoreCase("enchantments") || args[0].equalsIgnoreCase("e")) {
				
			}
			else if(args[0].equalsIgnoreCase("displayname") || args[0].equalsIgnoreCase("n")) {
				exchangeRule.setDisplayName(args[1]);
			}
			else if(args[0].equalsIgnoreCase("lore") || args[0].equalsIgnoreCase("l")) {
				exchangeRule.setLore(args[1].split(";"));
			}
			else if(args[0].equalsIgnoreCase("switchio") || args[0].equalsIgnoreCase("s")) {
				exchangeRule.switchIO();
			}
		}
		catch (ExchangeRuleParseException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
}
