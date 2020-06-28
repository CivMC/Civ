package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Syntax;
import com.programmerdan.minecraft.simpleadminhacks.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.AikarCommand;
import vg.civcraft.mc.civmodcore.command.AikarCommandManager;
import vg.civcraft.mc.civmodcore.util.EnumUtils;
import vg.civcraft.mc.civmodcore.util.TextUtil;

public class PlayerStatistics extends BasicHack {

	private AikarCommandManager commands;

	public PlayerStatistics(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}

	@Override
	public void registerCommands() {
		this.commands = new AikarCommandManager(plugin()) {
			@Override
			public void registerCommands() {
				registerCommand(new StatsCommand());
			}
		};
	}

	@Override
	public void unregisterCommands() {
		if (this.commands != null) {
			this.commands.reset();
			this.commands = null;
		}
	}

	@Override
	public String status() {
		return PlayerStatistics.class.getSimpleName() + " is " + (isEnabled() ? "enabled" : "disabled") + ".";
	}

	@CommandPermission("simpleadmin.stats")
	public static class StatsCommand extends AikarCommand {

		public static final String ALIAS = "stats|stat|statistic|statistics";

		@CommandAlias(StatsCommand.ALIAS)
		@Default
		public void fallback(CommandSender sender, @Optional String playerName) {
			sender.sendMessage("Available statistics commands:");
			sender.sendMessage(ChatColor.YELLOW + "get: " + ChatColor.WHITE + "/stats <player> <statistic>");
			sender.sendMessage(ChatColor.YELLOW + "set: " + ChatColor.WHITE + "/stats <player> <statistic> <value>");
		}

		@CommandAlias(StatsCommand.ALIAS)
		@Description("Gets the statistic for a player.")
		@Syntax("<player> <statistic>")
		@CommandCompletion("@players @stats")
		public void getPlayerStatistic(CommandSender sender, String playerName, @Single String statName) {
			Player player = Bukkit.getPlayer(playerName);
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "Could not find that player.");
				return;
			}
			Statistic statistic = EnumUtils.fromSlug(Statistic.class, statName, true);
			if (statistic == null) {
				sender.sendMessage(ChatColor.RED + "Could not find that statistic.");
				return;
			}
			sender.sendMessage(ChatColor.GOLD + player.getName() + " [" + statistic.name() + "]: " +
					ChatColor.WHITE + player.getStatistic(statistic));
		}

		@CommandAlias(StatsCommand.ALIAS)
		@Description("Gets the statistic for a player.")
		@Syntax("<player> <statistic>")
		@CommandCompletion("@players @stats")
		public void setPlayerStatistic(CommandSender sender, String playerName, String statName, int value) {
			Player player = Bukkit.getPlayer(playerName);
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "Could not find that player.");
				return;
			}
			Statistic statistic = EnumUtils.fromSlug(Statistic.class, statName, true);
			if (statistic == null) {
				sender.sendMessage(ChatColor.RED + "Could not find that statistic.");
				return;
			}
			player.setStatistic(statistic, value);
			sender.sendMessage(ChatColor.GOLD + player.getName() + " [" + statistic.name() + "] set to: " +
					ChatColor.WHITE + player.getStatistic(statistic));
		}

		@TabComplete("stats")
		public List<String> tabCompleteStatistics(BukkitCommandCompletionContext context) {
			List<String> results = new ArrayList<>();
			for (Statistic statistic : Statistic.values()) {
				String slug = statistic.name();
				if (!TextUtil.startsWith(slug, context.getInput())) {
					continue;
				}
				results.add(slug);
			}
			return results;
		}

	}

	public static BasicHackConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new BasicHackConfig(plugin, config);
	}

}
