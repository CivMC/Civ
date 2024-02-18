package com.untamedears.jukealert.util;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.SnitchManager;
import com.untamedears.jukealert.model.Snitch;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import vg.civcraft.mc.namelayer.permission.PermissionType;

// Static methods only
public final class JAUtility {

	private JAUtility() {

	}
	
	private static double tanPiDiv = Math.sqrt(2.0) - 1.0;

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
		SnitchManager snitchMan = JukeAlert.getInstance().getSnitchManager();
		Iterator<Block> itr = new BlockIterator(player, 40); // Within 2.5 chunks
		while (itr.hasNext()) {
			Block block = itr.next();
			Snitch found = snitchMan.getSnitchAt(block.getLocation());
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	public static TextComponent genTextComponent(Snitch snitch) {
		String name = snitch.getName().isEmpty() ? snitch.getType().getName() : snitch.getName();
		TextComponent textComponent = new TextComponent(ChatColor.AQUA + name);
		addSnitchHoverText(textComponent, snitch);
		return textComponent;
	}

	public static String genDirections(Snitch snitch, Player player) {
		if (snitch.getLocation().getWorld().equals(player.getLocation().getWorld())) {
			return String.format("%s[%sm %s%s%s]", ChatColor.GREEN, Math.round(player.getLocation().distance(snitch.getLocation())), ChatColor.RED, getCardinal(player.getLocation(), snitch.getLocation()), ChatColor.GREEN);
		} else {
			return ""; // Can't get directions to another world
		}
	}

	public static String getCardinal(Location start, Location end) {
		double dX = start.getBlockX() - end.getBlockX();
		double dZ = start.getBlockZ() - end.getBlockZ();

		if (Math.abs(dX) > Math.abs(dZ)) {
			if (Math.abs(dZ / dX) <= tanPiDiv) {
				return dX > 0 ? "West" : "East";
			} else if (dX > 0) {
				return dZ > 0 ? "North West" : "South West";
			} else {
				return dZ > 0 ? "North East" : "South East";
			}
		} else if (Math.abs(dZ) > 0) {
			if (Math.abs(dX / dZ) <= tanPiDiv) {
				return dZ > 0 ? "North" : "South";
			} else if (dZ > 0) {
				return dX > 0 ? "North West" : "North East";
			} else {
				return dX > 0 ? "South West" : "South East";
			}
		} else {
			return "";
		}
	}

	public static void addSnitchHoverText(TextComponent text, Snitch snitch) {
		StringBuilder sb = new StringBuilder();
		Location loc = snitch.getLocation();
		sb.append(String.format("%sLocation: %s(%s) [%d %d %d]%n", ChatColor.GOLD, ChatColor.AQUA,
				loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		if (!snitch.getName().isEmpty()) {
			sb.append(String.format("%sName: %s%s%n", ChatColor.GOLD, ChatColor.AQUA, snitch.getName()));
		}
		sb.append(String.format("%sGroup: %s%s", ChatColor.GOLD, ChatColor.AQUA, snitch.getGroup().getName()));
		text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(sb.toString())));
	}

	public static Material parseMaterial(String id) {
		try {
			return Material.valueOf(id);
		} catch (IllegalArgumentException e) {
			return Material.STONE;
		}
	}
	
	public static boolean isSameWorld(Location loc1, Location loc2) {
		return loc1.getWorld().getUID().equals(loc2.getWorld().getUID());
	}

	public static String formatLocation(Location location, boolean includeWorld) {
		if (includeWorld) {
			return String.format("[%s %d %d %d]", location.getWorld().getName(), location.getBlockX(), location.getBlockY(),
					location.getBlockZ());
		}
		return String.format("[%d %d %d]", location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	public static Material getVehicle(String vehicle) {
		switch (vehicle) {
			case "DONKEY":
				return Material.DONKEY_SPAWN_EGG;
			case "LLAMA":
				return Material.LLAMA_SPAWN_EGG;
			case "PIG":
				return Material.PIG_SPAWN_EGG;
			case "HORSE":
				return Material.HORSE_SPAWN_EGG;
			case "MINECART":
				return Material.MINECART;
			case "MINECART_CHEST":
				return Material.CHEST_MINECART;
			case "MINECART_FURNACE":
				return Material.FURNACE_MINECART;
			case "MINECART_TNT":
				return Material.TNT_MINECART;
			case "MINECART_HOPPER":
				return Material.HOPPER_MINECART;
			case "STRIDER":
				return Material.STRIDER_SPAWN_EGG;
			case "BOAT":
			case "OAK_BOAT":
				return Material.OAK_BOAT;
			case "BIRCH_BOAT":
				return Material.BIRCH_BOAT;
			case "SPRUCE_BOAT":
				return Material.SPRUCE_BOAT;
			case "JUNGLE_BOAT":
				return Material.JUNGLE_BOAT;
			case "ACACIA_BOAT":
				return Material.ACACIA_BOAT;
			case "DARK_OAK_BOAT":
				return Material.DARK_OAK_BOAT;
			default:
				JukeAlert.getInstance().getLogger().info("Failed to parse vehicle into material: " + vehicle);
				return Material.STONE;
		}
	}
}
