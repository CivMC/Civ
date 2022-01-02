package com.untamedears.itemexchange.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.untamedears.itemexchange.ItemExchangeConfig;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

@CommandAlias("iei|ieinfo")
public final class InfoCommand extends BaseCommand {

	@Subcommand("shop|shopblocks")
	@Description("Shows what blocks can be made into shops.")
	public void onShopBlocksInfo(CommandSender sender) {
		Set<Material> shopBlocks = ItemExchangeConfig.getShopCompatibleBlocks();
		if (shopBlocks.isEmpty()) {
			sender.sendMessage(ChatColor.GOLD + "ItemExchange has no configured shop blocks.");
		}
		else {
			sender.sendMessage(ChatColor.GOLD + "ItemExchange shops can be made from the following blocks:");
			for (Material material : shopBlocks) {
				sender.sendMessage(" - " + material.name());
			}
		}
	}

	@Subcommand("button|successbutton")
	@Description("Shows what shop blocks can trigger a successful transaction button.")
	public void onSuccessButtonInfo(CommandSender sender) {
		Set<Material> successBlocks = ItemExchangeConfig.getSuccessButtonBlocks();
		if (successBlocks.isEmpty()) {
			sender.sendMessage(ChatColor.GOLD + "ItemExchange has no configured success button shop blocks.");
		}
		else {
			sender.sendMessage(ChatColor.GOLD + "ItemExchange has the following success button shop blocks:");
			for (Material material : successBlocks) {
				sender.sendMessage(" - " + material.name());
			}
		}
	}

	@Subcommand("rule|ruleitem")
	@Description("Shows what material exchange rules will be made out of.")
	public void onRuleItemInfo(CommandSender sender) {
		sender.sendMessage(ChatColor.GOLD + "ItemExchange exchange rules will be made from: " + ChatColor.WHITE
				+ ItemExchangeConfig.getRuleItemMaterial());
	}

}
