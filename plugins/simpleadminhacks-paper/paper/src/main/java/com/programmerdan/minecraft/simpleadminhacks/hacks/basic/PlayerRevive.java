package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Syntax;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.commands.CommandManager;

public final class PlayerRevive extends BasicHack {

	private final CommandManager commands;

	public PlayerRevive(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
		this.commands = new CommandManager(plugin) {
			@Override
			public void registerCommands() {
				registerCommand(new ReviveCommand());
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

	@CommandPermission("simpleadmin.revive")
	public static class ReviveCommand extends BaseCommand {

		@CommandAlias("revive|respawn|resurrect|ress")
		@Syntax("<player name>")
		@Description("Revives a player")
		@CommandCompletion("@players")
		public void revivePlayer(final CommandSender sender, @Single final String name) {
			final Player player = Bukkit.getPlayer(name);
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "That player doesn't exist or isn't online.");
				return;
			}
			if (!player.isDead()) {
				sender.sendMessage(ChatColor.RED + "That player is not dead.");
				return;
			}
			final Location prevBedSpawn = player.getBedSpawnLocation();
			player.setBedSpawnLocation(player.getLocation(), true);
			player.spigot().respawn();
			player.setBedSpawnLocation(prevBedSpawn, true);
			player.sendMessage(ChatColor.YELLOW + "You have been revived.");
			sender.sendMessage(player.getName() + " revived.");
		}

	}

}
