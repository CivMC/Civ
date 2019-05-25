package com.untamedears.JukeAlert.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.manager.GlobalSnitchManager;
import com.untamedears.JukeAlert.model.Snitch;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.namelayer.permission.PermissionType;

// Static methods only
public class Utility {
	
	private Utility() {
		
	}

	public static Snitch findClosestSnitch(Location loc, PermissionType perm, UUID player) {
		Snitch closestSnitch = null;
		double closestDistance = Double.MAX_VALUE;
		Collection<Snitch> snitches = JukeAlert.getInstance().getSnitchManager().getSnitchesCovering(loc);
		for (Snitch snitch : snitches) {
			if (snitch.hasPermission(player, perm)) {
				double distance = snitch.getLocation().distanceSquared(loc);
				if (distance < closestDistance) {
					closestDistance = distance;
					closestSnitch = snitch;
				}
			}
		}
		return closestSnitch;
	}

	public static Snitch findLookingAtOrClosestSnitch(Player player, PermissionType perm) {

		Snitch cursorSnitch = getSnitchUnderCursor(player);
		if (cursorSnitch != null && cursorSnitch.hasPermission(player, perm)) {
			return cursorSnitch;
		}
		return findClosestSnitch(player.getLocation(), perm, player.getUniqueId());
	}

	public static Snitch getSnitchUnderCursor(Player player) {
		GlobalSnitchManager manager = JukeAlert.getInstance().getSnitchManager();
		Iterator<Block> itr = new BlockIterator(player, 40); // Within 2.5 chunks
		while (itr.hasNext()) {
			Block block = itr.next();
			Snitch found = manager.getSnitchAt(block.getLocation());
			if (found != null) {
				return found;
			}
		}
		return null;
	}
	
	public static TextComponent genTextComponent(Snitch snitch) {
		String name = snitch.getName().isEmpty() ? "Snitch" : snitch.getName();
		TextComponent textComponent = new TextComponent(name);
		addSnitchHoverText(textComponent, snitch);
		return textComponent;
	}

	public static void addSnitchHoverText(TextComponent text, Snitch snitch) {
		StringBuilder sb = new StringBuilder();
		Location loc = snitch.getLocation();
		sb.append(String.format("%sLocation: %s(%s) [%d %d %d]%n", ChatColor.GOLD, ChatColor.AQUA,
				loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		if (!snitch.getName().isEmpty()) {
			sb.append(String.format("%sName: %s%s%n", ChatColor.GOLD, ChatColor.AQUA, snitch.getName()));
		}
		sb.append(String.format("%sGroup: %s%s%n", ChatColor.GOLD, ChatColor.AQUA, snitch.getGroup().getName()));
		text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(sb.toString()).create()));
	}
}
