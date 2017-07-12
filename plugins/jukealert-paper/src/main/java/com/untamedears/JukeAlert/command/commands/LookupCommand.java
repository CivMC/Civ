package com.untamedears.JukeAlert.command.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.model.Snitch;

public class LookupCommand extends PlayerCommand {

	private SnitchManager snitchManager;

	public LookupCommand() {

		super("Lookup");
		setDescription("Lookup a snitch's group by its coordinates");
		setUsage("/jalookup <x> <y> <z> [world]");
		setArguments(3, 4);
		setIdentifier("jalookup");
		snitchManager = JukeAlert.getInstance().getSnitchManager();
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {

		boolean canLookupAny = (
			sender instanceof ConsoleCommandSender || sender.hasPermission("jukealert.admin.lookupany"));
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (player != null || canLookupAny) {
			int x;
			int y;
			int z;
			String world;
			try {
				x = Integer.parseInt(args[0]);
				y = Integer.parseInt(args[1]);
				z = Integer.parseInt(args[2]);
				if (args.length == 3) {
					world = player.getLocation().getWorld().getName();
				} else {
					world = args[3];
				}
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Invalid coordinates.");
				return false;
			}
			if (Bukkit.getWorld(world) == null) {
				sender.sendMessage(ChatColor.RED + "Invalid world.");
				return false;
			}
			Location loc = new Location(Bukkit.getWorld(world), x, y, z);
			Snitch snitch = snitchManager.getSnitch(loc.getWorld(), loc);
			if (snitch == null) {
				sender.sendMessage(ChatColor.RED + "You do not own a snitch at those coordinates!");
				return false;
			}
			if (canLookupAny || (
					player != null
					&& NameAPI.getGroupManager().hasAccess(
						snitch.getGroup(), player.getUniqueId(), PermissionType.getPermission("LOOKUP_SNITCH")))) {
				TextComponent playerSnitchInfoMessage = new TextComponent(ChatColor.AQUA
					+ "The snitch at [" + x + " " + y + " " + z + "] is on group "
					+ snitch.getGroup().getName());
				String hoverText = snitch.getHoverText(null, null);
				playerSnitchInfoMessage.setHoverEvent(
					new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
				player.spigot().sendMessage(playerSnitchInfoMessage);
			} else {
				sender.sendMessage(ChatColor.RED + "You don't have permission to lookup the group of this snitch");
			}
			return true;
		} else {
			sender.sendMessage(ChatColor.RED + "You do not own a snitch at those coordinates!");
			return false;
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {

		return null;
	}
}
