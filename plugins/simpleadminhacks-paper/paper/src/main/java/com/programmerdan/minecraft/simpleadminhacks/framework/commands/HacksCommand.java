package com.programmerdan.minecraft.simpleadminhacks.framework.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Syntax;
import com.google.common.base.Strings;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

@CommandPermission(CommandRegistrar.PERMISSION_HACKS)
public class HacksCommand extends BaseCommand {

	protected final SimpleAdminHacks plugin;

	public HacksCommand(SimpleAdminHacks plugin) {
		this.plugin = plugin;
	}

	@CommandAlias(CommandRegistrar.ROOT_ALIAS)
	@Description("Views the status of a specific hack.")
	public void viewHacksCommand(final CommandSender sender) {
		sender.sendMessage("Hacks:");
		final List<SimpleHack<? extends SimpleHackConfig>> hacks = this.plugin.getHackManager().getHacks();
		if (hacks.isEmpty()) {
			sender.sendMessage(" No hacks registered.");
		}
		else {
			for (final SimpleHack<? extends SimpleHackConfig> hack : hacks) {
				sender.sendMessage(" • " + ChatColor.YELLOW + hack.getName() + ": " + ChatColor.AQUA +
						(hack.isEnabled() ? "enabled" : "disabled"));
			}
		}
	}

	@CommandAlias(CommandRegistrar.ROOT_ALIAS)
	@CommandCompletion("@hacks")
	@Syntax("<hack>")
	@Description("Views the status of a specific hack.")
	public void getStatusCommand(final CommandSender sender, @Single final String hack) {
		final SimpleHack<? extends SimpleHackConfig> target = this.plugin.getHackManager().getHack(hack);
		if (target == null) {
			sender.sendMessage(ChatColor.RED + "That hack could not be found.");
			return;
		}
		sender.sendMessage(ChatColor.WHITE + target.getName() + ":");
		sender.sendMessage(" " + target.status());
	}

	@CommandAlias(CommandRegistrar.ROOT_ALIAS)
	@CommandCompletion("@hacks enable|disable")
	@Syntax("<hack> <enable/disable>")
	@Description("Enables or disables a specific hack.")
	public void setStateCommand(final CommandSender sender, String hack, @Single final String action) {
		final SimpleHack<? extends SimpleHackConfig> target = this.plugin.getHackManager().getHack(hack);
		if (target == null) {
			sender.sendMessage(ChatColor.RED + "That hack could not be found.");
			return;
		}
		hack = target.getClass().getSimpleName();
		switch (action.toLowerCase()) {
			case "enable":
			case "enabled":
			case "start":
			case "yes":
			case "y": {
				if (target.isEnabled()) {
					sender.sendMessage(ChatColor.GOLD + hack + " is already enabled.");
					return;
				}
				target.enable();
				if (!target.isEnabled()) {
					sender.sendMessage(ChatColor.RED + hack + " could not be enabled!");
					return;
				}
				sender.sendMessage(ChatColor.GREEN + hack + " is now enabled.");
				return;
			}
			case "disable":
			case "disabled":
			case "end":
			case "exit":
			case "quit":
			case "kill":
			case "no":
			case "n": {
				if (!target.isEnabled()) {
					sender.sendMessage(ChatColor.GOLD + hack + " is not enabled.");
					return;
				}
				target.disable();
				if (target.isEnabled()) {
					sender.sendMessage(ChatColor.GOLD + hack + " could not be disabled!");
					return;
				}
				sender.sendMessage(ChatColor.GREEN + hack + " has been disabled.");
				return;
			}
			default: {
				throw new InvalidCommandArgument("Please specify whether to enable to disable the hack.");
			}
		}
	}

	@CommandAlias(CommandRegistrar.ROOT_ALIAS)
	@CommandCompletion("@hacks get|set @nothing")
	@Syntax("<hack> <get/set> <key> [value]")
	@Description("Gets or sets a specific value for a particular hack.")
	public void getSetValueCommand(final CommandSender sender, final String hack, final String action,
								   final String key, @Optional final String value) {
		if (Strings.isNullOrEmpty(key)) {
			throw new InvalidCommandArgument("You need to pass a key.");
		}
		final SimpleHack<? extends SimpleHackConfig> target = this.plugin.getHackManager().getHack(hack);
		if (target == null) {
			sender.sendMessage(ChatColor.RED + "That hack could not be found.");
			return;
		}
		final SimpleHackConfig config = target.config();
		switch (action.toLowerCase()) {
			case "get": {
				if (!config.has(key)) {
					sender.sendMessage(target.getName() + ": " + ChatColor.YELLOW + "No value for \"" + key +
							"\" has been set.");
					return;
				}
				sender.sendMessage(target.getName() + ": " +
						ChatColor.YELLOW + "\"" + key + "\" == \"" + config.get(key) + "\"");
				return;
			}
			case "set": {
				try {
					config.set(key, value);
					sender.sendMessage(target.getName() + ": " + ChatColor.YELLOW + "\"" + key + "\" = \"" +
							value + "\"");
					return;
				}
				catch (Exception exception) {
					sender.sendMessage(target.getName() + ": " + ChatColor.RED + "Failed to set \"" + key +
							"\" to \"" + value + "\"");
					return;
				}
			}
			default: {
				throw new InvalidCommandArgument("Please specify whether to get to set the hack value.");
			}
		}
	}

}
