package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Syntax;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.commands.CommandManager;
import vg.civcraft.mc.civmodcore.commands.TabComplete;

public final class PlayerStatistics extends BasicHack {

	private final CommandManager commands;

	public PlayerStatistics(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
		this.commands = new CommandManager(plugin()) {
			@Override
			public void registerCommands() {
				registerCommand(new StatsCommand());
			}
		};
	}

	@Override
	public void onEnable() {
		super.onEnable();
		this.commands.init();
	}

	@Override
	public void onDisable() {
		this.commands.reset();
		super.onDisable();
	}

	@CommandPermission("simpleadmin.stats")
	public static class StatsCommand extends BaseCommand {

		public static final String ALIAS = "stats|stat|statistic|statistics";

		@CommandAlias(StatsCommand.ALIAS)
		@Default
		public void fallback(final CommandSender sender, @Optional final String playerName) {
			sender.sendMessage(ChatColor.RED + "Available statistics commands:");
			sender.sendMessage(ChatColor.YELLOW + "get: " + ChatColor.WHITE + "/stats <player> <statistic>");
			sender.sendMessage(ChatColor.YELLOW + "set: " + ChatColor.WHITE + "/stats <player> <statistic> <value>");
		}

		@CommandAlias(StatsCommand.ALIAS)
		@Description("Gets the statistic for a player.")
		@Syntax("<player> <statistic>")
		@CommandCompletion("@players @stats")
		public void getPlayerStatistic(final CommandSender sender,
									   final String playerName,
									   @Single final String statName) {
			final Player player = Bukkit.getPlayer(playerName);
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "Could not find that player.");
				return;
			}
			final Statistic statistic = EnumUtils.getEnumIgnoreCase(Statistic.class, statName);
			if (statistic == null) {
				sender.sendMessage(ChatColor.RED + "Could not find that statistic.");
				return;
			}
			sender.sendMessage(ChatColor.GOLD + player.getName() + " [" + statistic.name() + "]: " +
					ChatColor.WHITE + player.getStatistic(statistic));
		}

		@CommandAlias(StatsCommand.ALIAS)
		@Description("Sets the statistic for a player.")
		@Syntax("<player> <statistic> <value>")
		@CommandCompletion("@players @stats @nothing")
		public void setPlayerStatistic(final CommandSender sender,
									   final String playerName,
									   final String statName,
									   final int value) {
			final Player player = Bukkit.getPlayer(playerName);
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "Could not find that player.");
				return;
			}
			final Statistic statistic = EnumUtils.getEnumIgnoreCase(Statistic.class, statName);
			if (statistic == null) {
				sender.sendMessage(ChatColor.RED + "Could not find that statistic.");
				return;
			}
			player.setStatistic(statistic, value);
			sender.sendMessage(ChatColor.GOLD + player.getName() + " [" + statistic.name() + "] set to: " +
					ChatColor.WHITE + player.getStatistic(statistic));
		}

		@TabComplete("stats")
		public List<String> tabCompleteStatistics(final BukkitCommandCompletionContext context) {
			final List<String> results = new ArrayList<>();
			for (final Statistic statistic : Statistic.values()) {
				final String slug = statistic.name();
				if (!StringUtils.startsWith(slug, context.getInput())) {
					continue;
				}
				results.add(slug);
			}
			return results;
		}

	}

}
