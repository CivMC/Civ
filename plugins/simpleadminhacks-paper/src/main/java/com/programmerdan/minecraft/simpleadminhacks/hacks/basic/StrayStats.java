package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import vg.civcraft.mc.civmodcore.commands.CommandManager;

public final class StrayStats extends BasicHack {

	// ------------------------------------------------------------
	// Bootstrap
	// ------------------------------------------------------------

	private final CommandManager commands;

	public StrayStats(final SimpleAdminHacks plugin, final BasicHackConfig config) {
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
		if(plugin.getServer().getPluginManager().getPlugin("BanStick") == null) {
			plugin.severe("StrayStats failed to initialize because BanStick plugin is not found");
			return;
		}
		commands.init();
	}

	@Override
	public void onDisable() {
		commands.reset();
		super.onDisable();
	}

	// ------------------------------------------------------------
	// Statistics Commands
	// ------------------------------------------------------------

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	private class StatsCommand extends BaseCommand {
		@CommandAlias("compile_player_join_statistics")
		@CommandPermission("simpleadmin.stats")
		public void compileStatistics(final CommandSender sender) {
			sender.sendMessage(ChatColor.GOLD + "Starting to compile player join statistics!");
			Bukkit.getScheduler().runTaskAsynchronously(plugin(), () -> {
				try (final var file = new FileWriter(new File(plugin().getDataFolder(), "playerJoinStats.csv"))) {
					try (final var connection = BanStickDatabaseHandler.getInstanceData().getConnection();
						 final var statement = connection.prepareStatement(
								 "SELECT name, first_add FROM bs_player ORDER BY first_add ASC");
						 final var query = statement.executeQuery();) {

						while (query.next()) {
							file.write(query.getString(1) + "," + DATE_FORMAT.format(query.getTimestamp(2)) + "\n");
						}
					}
				}
				catch (final IOException | SQLException exception) {
					plugin().warning("Could not complete compilation!", exception);
					sender.sendMessage(ChatColor.RED + "Could not complete compilation!");
					return;
				}
				sender.sendMessage(ChatColor.GREEN + "Finished compiling player join statistics!");
			});
		}
	}

}
