package com.untamedears.JukeAlert.command.commands;

import static com.untamedears.JukeAlert.util.Utility.findLookingAtOrClosestSnitch;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.Snitch;

import org.bukkit.Bukkit;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class ClearCommand extends PlayerCommand {

	public ClearCommand() {

		super("Clear");
		setDescription("Clears snitch logs");
		setUsage("/jaclear");
		setArguments(0, 0);
		setIdentifier("jaclear");
	}

	@Override
	public boolean execute(final CommandSender sender, String[] args) {

		if (sender instanceof Player) {
			Player player = (Player) sender;
			final Snitch snitch = findLookingAtOrClosestSnitch(player, PermissionType.getPermission("CLEAR_SNITCHLOG"));
			if (snitch != null) {
				Bukkit.getScheduler().runTaskAsynchronously(JukeAlert.getInstance(), new Runnable() {
					@Override
					public void run() {
						deleteLog(sender, snitch);
					}
				});
			   return true;
			} else {
				sender.sendMessage(
					ChatColor.RED + "You do not own any snitches nearby or lack permission to delete their logs!");
				return true;
			}
		} else {
			sender.sendMessage("You must be a player!");
			return false;
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {

		return null;
	}

	public static void deleteLog(CommandSender sender, Snitch snitch) {

		final Player player = (Player) sender;
		final Boolean completed = JukeAlert.getInstance().getJaLogger().deleteSnitchInfo(snitch.getId());
		// Only send messages sync
		new BukkitRunnable() {
			@Override
			public void run() {
				TextComponent playerSnitchInfoMessage;
				if (completed) {
					playerSnitchInfoMessage = new TextComponent(ChatColor.AQUA + "Cleared all snitch logs");
				} else {
					playerSnitchInfoMessage = new TextComponent(ChatColor.DARK_RED + "Snitch Clear Failed");
				}
				String hoverText = snitch.getHoverText(null, null);
				playerSnitchInfoMessage.setHoverEvent(
					new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
				player.spigot().sendMessage(playerSnitchInfoMessage);
			}
		}.runTask(JukeAlert.getInstance());
	}
}
