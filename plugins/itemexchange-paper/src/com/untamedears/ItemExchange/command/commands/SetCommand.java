package com.untamedears.ItemExchange.command.commands;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.ItemExchange.command.PlayerCommand;
import com.untamedears.ItemExchange.utility.ExchangeRule;
import com.untamedears.ItemExchange.exceptions.ExchangeRuleParseException;
import com.untamedears.ItemExchange.utility.InteractionResponse;
import com.untamedears.ItemExchange.utility.InteractionResponse.InteractionResult;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/*
 * When holding an exchange rule block in the players hand allowes editing of the 
 * different rules.
 */
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
			if((args[0].equalsIgnoreCase("commonname") || args[0].equalsIgnoreCase("c"))&&args.length==2){
				ItemStack itemStack=ItemExchangePlugin.NAME_MATERIAL.get(args[1]);
				exchangeRule.setMaterial(itemStack.getType());
				exchangeRule.setDurability(itemStack.getDurability());
			}
			else if((args[0].equalsIgnoreCase("material") || args[0].equalsIgnoreCase("m"))&&args.length==2){
				exchangeRule.setMaterial(Material.getMaterial(args[1]));
			}
			else if((args[0].equalsIgnoreCase("amount") || args[0].equalsIgnoreCase("a"))&&args.length==2){
				exchangeRule.setAmount(Integer.valueOf(args[1]));
			}
			else if((args[0].equalsIgnoreCase("durability") || args[0].equalsIgnoreCase("d"))&&args.length==2) {
				exchangeRule.setDurability(Short.valueOf(args[1]));
			}
			else if((args[0].equalsIgnoreCase("enchantment") || args[0].equalsIgnoreCase("e"))&&args.length==3) {
				Enchantment enchantment=Enchantment.getByName(ItemExchangePlugin.ABBRV_ENCHANTMENT.get(args[1].substring(1, args[1].length()-1)));
				Integer level=Character.getNumericValue(args[1].charAt(args[1].length()-1));
				if(args[2].equalsIgnoreCase("required") || args[2].equalsIgnoreCase("r")){
					exchangeRule.requireEnchantment(enchantment, level);
				}
				else if(args[2].equalsIgnoreCase("excluded") || args[2].equalsIgnoreCase("e")){
					exchangeRule.excludeEnchantment(enchantment, level);
				}
			}
			else if((args[0].equalsIgnoreCase("displayname") || args[0].equalsIgnoreCase("n"))&&args.length==2){
				exchangeRule.setDisplayName(args[1]);
			}
			else if((args[0].equalsIgnoreCase("lore") || args[0].equalsIgnoreCase("l"))&&args.length==2) {
				exchangeRule.setLore(args[1].split(";"));
			}
			else if(args[0].equalsIgnoreCase("switchio") || args[0].equalsIgnoreCase("s")) {
				exchangeRule.switchIO();
			}
		}
		catch (ExchangeRuleParseException e) {
			InteractionResponse.messagePlayerResult((Player)sender, new InteractionResponse(InteractionResult.FAILURE,"You are not holding an exchange rule."));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
}
