package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Syntax;
import com.programmerdan.minecraft.simpleadminhacks.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.AikarCommand;
import vg.civcraft.mc.civmodcore.command.AikarCommandManager;

public class PlayerRevive extends BasicHack {

	private AikarCommandManager commands;

	public PlayerRevive(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}

	@Override
	public void registerCommands() {
		this.commands = new AikarCommandManager(plugin()) {
			@Override
			public void registerCommands() {
				registerCommand(new ReviveCommand());
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
		return PlayerRevive.class.getSimpleName() + " is " + (isEnabled() ? "enabled" : "disabled") + ".";
	}

	@CommandPermission("simpleadmin.revive")
	public static class ReviveCommand extends AikarCommand {

		@CommandAlias("revive|respawn|resurrect|ress")
		@Syntax("<player name>")
		@Description("Revives a player")
		@CommandCompletion("@players")
		public void revivePlayer(CommandSender sender, @Single String name) {
			Player player = Bukkit.getPlayer(name);
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "That player doesn't exist or isn't online.");
				return;
			}
			if (!player.isDead()) {
				sender.sendMessage(ChatColor.RED + "That player is not dead.");
				return;
			}
			Location prevBedSpawn = player.getBedSpawnLocation();
			player.setBedSpawnLocation(player.getLocation(), true);
			player.spigot().respawn();
			player.setBedSpawnLocation(prevBedSpawn, true);
			player.sendMessage(ChatColor.YELLOW + "You have been revived.");
			sender.sendMessage(player.getName() + " revived.");
		}

	}

	public static BasicHackConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new BasicHackConfig(plugin, config);
	}

}
