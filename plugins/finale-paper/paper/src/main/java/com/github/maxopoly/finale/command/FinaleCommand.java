package com.github.maxopoly.finale.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.CombatConfig;
import com.github.maxopoly.finale.misc.BlockRestrictionHandler;
import com.github.maxopoly.finale.misc.crossbow.AntiAirMissile;
import com.github.maxopoly.finale.misc.crossbow.CrossbowHandler;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@CommandAlias("finale")
public class FinaleCommand extends BaseCommand {

	@Subcommand("hunger")
	@Description("Set hunger.")
	@CommandPermission("finale.op")
	public void setHunger(Player sender, @Optional Player target, int hunger) {
		Player playerTarget = target;
		if (playerTarget == null) {
			playerTarget = sender;
		}
		playerTarget.setFoodLevel(hunger);
		sender.sendMessage("Setting " + playerTarget.getName() + "'s hunger level to " + hunger);
	}

	@Subcommand("block restrictions")
	@Description("All you need to know on block restrictions in combat.")
	@CommandPermission("finale.blocks")
	public void blockList(Player sender) {
		BlockRestrictionHandler blockRestrictionHandler = Finale.getPlugin().getManager().getBlockRestrictionHandler();
		BlockRestrictionHandler.RestrictionMode mode = blockRestrictionHandler.getMode();
		sender.sendMessage("Restriction Mode: " + mode);
		switch (mode) {
			case WHITELIST:
				blockWhitelist(sender);
				break;
			case BLACKLIST:
				blockBlacklist(sender);
				break;
			default:
				break;
		}
		blockCooldowns(sender);
	}

	@Subcommand("block blacklist")
	@Description("View blacklisted blocks while in combat.")
	@CommandPermission("finale.blocks")
	public void blockBlacklist(Player sender) {
		BlockRestrictionHandler blockRestrictionHandler = Finale.getPlugin().getManager().getBlockRestrictionHandler();
		List<Material> blacklist = blockRestrictionHandler.getBlacklist();
		sender.sendMessage("Block Restriction Blacklist: ");
		sendMaterialList(sender, blacklist);
	}

	@Subcommand("block whitelist")
	@Description("View whitelisted blocks while in combat.")
	@CommandPermission("finale.blocks")
	public void blockWhitelist(Player sender) {
		BlockRestrictionHandler blockRestrictionHandler = Finale.getPlugin().getManager().getBlockRestrictionHandler();
		List<Material> whitelist = blockRestrictionHandler.getWhitelist();
		sender.sendMessage("Block Restriction Whitelist: ");
		sendMaterialList(sender, whitelist);
	}

	@Subcommand("block cooldowns")
	@Description("View cooldowns on blocks while in combat.")
	@CommandPermission("finale.blocks")
	public void blockCooldowns(Player sender) {
		BlockRestrictionHandler blockRestrictionHandler = Finale.getPlugin().getManager().getBlockRestrictionHandler();
		Map<Material, Long> materialCooldowns = blockRestrictionHandler.getMaterialCooldowns();
		sender.sendMessage("Block Restriction Cooldowns: ");
		for (Map.Entry<Material, Long> materialCooldownEntry : materialCooldowns.entrySet()) {
			sender.sendMessage(materialCooldownEntry.getKey() + ": " + materialCooldownEntry.getValue());
		}
	}

	private void sendMaterialList(Player sender, List<Material> materials) {
		for (Material material : materials) {
			sender.sendMessage(material.toString());
		}
	}

	@Subcommand("give aa")
	@Description("Give a Finale anti air item.")
	@CommandPermission("finale.give")
	public void give(Player sender, String key, int amount) {
		CrossbowHandler crossbowHandler = Finale.getPlugin().getManager().getCrossbowHandler();
		AntiAirMissile antiAirMissile = crossbowHandler.getAntiAirMissile(key);
		ItemStack is = antiAirMissile.getItemStack();
		is.setAmount(amount);
		sender.getInventory().addItem(is);
		sender.sendMessage("Given you " + amount + " " + is.getI18NDisplayName());
	}

	@Subcommand("chemtrails")
	@Description("Turn on chemtrails.")
	@CommandPermission("finale.chemtrails")
	public void toggleChemtrails(Player sender) {
		Set<UUID> chemtrails = Finale.getPlugin().getManager().getChemtrails();
		if (chemtrails.contains(sender.getUniqueId())) {
			chemtrails.remove(sender.getUniqueId());
		} else {
			chemtrails.add(sender.getUniqueId());
		}
		sender.sendMessage("Chemtrails: " + chemtrails.contains(sender.getUniqueId()));
	}

}
