package com.untamedears.itemexchange.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.untamedears.itemexchange.ItemExchangePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import vg.civcraft.mc.civmodcore.command.AikarCommand;

@CommandAlias("ier|iereload")
@CommandPermission("itemexchange.reload")
public class ReloadCommand extends AikarCommand {

	private final ItemExchangePlugin plugin;

	public ReloadCommand(ItemExchangePlugin plugin) {
		this.plugin = plugin;
	}

	@Default
	@Description("Reload's ItemExchange's config.")
	public void onReloadConfig(CommandSender sender) {
		this.plugin.saveDefaultConfig();
		this.plugin.config().reset();
		this.plugin.reloadConfig();
		this.plugin.config().parse();
		sender.sendMessage(ChatColor.GREEN + "ItemExchange's config has been reloaded.");
	}

}
