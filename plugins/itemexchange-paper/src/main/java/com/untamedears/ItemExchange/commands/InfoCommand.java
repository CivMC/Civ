package com.untamedears.itemexchange.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.untamedears.itemexchange.ItemExchangePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import vg.civcraft.mc.civmodcore.command.AikarCommand;

@CommandAlias("iei|ieinfo")
public class InfoCommand extends AikarCommand {

	@Default
	public void onDefault() {

	}

	@Subcommand("shop|shopblocks")
	public void onShopBlocksInfo(CommandSender sender) {
		if (ItemExchangePlugin.SHOP_BLOCKS.isEmpty()) {
			sender.sendMessage(ChatColor.GOLD + "ItemExchange has no configured shop blocks.");
		}
		else {
			sender.sendMessage(ChatColor.GOLD + "ItemExchange has the following shop blocks:");
			for (Material material : ItemExchangePlugin.SHOP_BLOCKS) {
				sender.sendMessage(" - " + material.name());
			}
		}
	}

	@Subcommand("button|successbutton")
	public void onSuccessButtonInfo(CommandSender sender) {
		if (ItemExchangePlugin.SUCCESS_BUTTON_BLOCKS.isEmpty()) {
			sender.sendMessage(ChatColor.GOLD + "ItemExchange has no configured success button shop blocks.");
		}
		else {
			sender.sendMessage(ChatColor.GOLD + "ItemExchange has the following success button shop blocks:");
			for (Material material : ItemExchangePlugin.SUCCESS_BUTTON_BLOCKS) {
				sender.sendMessage(" - " + material.name());
			}
		}
	}

	@Subcommand("rule|ruleitem")
	public void onRuleItemInfo(CommandSender sender) {
		sender.sendMessage(ChatColor.GOLD + "ItemExchange exchange rules will be made from: " + ChatColor.WHITE +
				ItemExchangePlugin.RULE_ITEM.getType());
	}

}
