package com.untamedears.itemexchange.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.utility.RuleHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

@CommandAlias(SetCommand.ALIAS)
public final class SetCommand extends BaseCommand {

	public static final String ALIAS = "ies|ieset|set";

	@CatchUnknown
	@Description("Sets a pertinent field to an exchange rule.")
	@Syntax("<field> [...values]")
	public void base(Player player) {
		throw new InvalidCommandArgument();
	}

	@Subcommand("material|mat|m")
	@Description("Sets the material of an exchange rule.")
	@Syntax("<material>")
	@CommandCompletion("@itemMaterials")
	public void setMaterial(Player player, @Single String slug) {
		try (RuleHandler handler = new RuleHandler(player)) {
			Material material = Material.getMaterial(slug.toUpperCase());
			if (!ItemUtils.isValidItemMaterial(material)) {
				throw new InvalidCommandArgument("You must enter a valid item material.");
			}
			handler.getRule().setMaterial(material);
			handler.relay(ChatColor.GREEN + "Material successfully changed.");
		}
	}

	@Subcommand("amount|num|number|a")
	@Description("Sets the amount of an exchange rule.")
	@Syntax("<amount>")
	public void setAmount(Player player, int amount) {
		try (RuleHandler handler = new RuleHandler(player)) {
			if (amount <= 0) {
				throw new InvalidCommandArgument("You must enter a valid amount.");
			}
			handler.getRule().setAmount(amount);
			handler.relay(ChatColor.GREEN + "Amount successfully changed.");
		}
	}

	@Subcommand("switchio|switch|swap|swapio")
	@Description("Sets the amount of an exchange rule.")
	public void switchIO(Player player) {
		try (RuleHandler handler = new RuleHandler(player)) {
			if (handler.getRule().getType() == ExchangeRule.Type.INPUT) {
				handler.getRule().setType(ExchangeRule.Type.OUTPUT);
				handler.relay(ChatColor.GREEN + "Rule switched to an output.");
			}
			else if (handler.getRule().getType() == ExchangeRule.Type.OUTPUT) {
				handler.getRule().setType(ExchangeRule.Type.INPUT);
				handler.relay(ChatColor.GREEN + "Rule switched to an input.");
			}
			else {
				handler.relay(ChatColor.RED + "Rule could not be switched.");
			}
		}
	}

}
