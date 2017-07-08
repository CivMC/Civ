package com.untamedears.JukeAlert.util;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import net.md_5.bungee.api.chat.TextComponent;

import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.model.Snitch;

// Static methods only
public class Utility {

	private static boolean debugging_ = false;

	public static boolean isDebugging() {

		return debugging_;
	}

	public static void setDebugging(boolean debugging) {

		debugging_ = debugging;
	}

	public static void notifyGroup(Group g, TextComponent message) throws SQLException {

		if (g == null) {
			return;
		}
		final JukeAlert plugin = JukeAlert.getInstance();
		Set<String> skipUUID = plugin.getJaLogger().getIgnoreUUIDs(g.getName());
		if (skipUUID == null) {
			// This should be fine as it is how it used to be done
			skipUUID = null;
		}
		OnlineGroupMembers iter = OnlineGroupMembers.get(g.getName()).skipList(skipUUID);
		for (Player player : iter) {
			if (NameAPI.getGroupManager().hasAccess(g, player.getUniqueId(),
					PermissionType.getPermission("SNITCH_NOTIFICATIONS"))) {
				RateLimiter.sendMessage(player, message);
			}
		}
	}

	public static void notifyGroup(Snitch snitch, TextComponent message) throws SQLException {

		Group sG = snitch.getGroup();
		if (sG == null) {
			return;
		}
		final JukeAlert plugin = JukeAlert.getInstance();
		Set<String> skipUUID = plugin.getJaLogger().getIgnoreUUIDs(sG.getName());
		if (skipUUID == null) {
			// This should be fine as it is how it used to be done
			skipUUID = null;
		}
		OnlineGroupMembers iter = OnlineGroupMembers.get(sG.getName()).reference(snitch.getLoc()).skipList(skipUUID);
		if (!snitch.shouldLog()) {
			iter.maxDistance(JukeAlert.getInstance().getConfigManager().getMaxAlertDistanceNs());
		}
		for (Player player : iter) {
			if (NameAPI.getGroupManager().hasAccess(snitch.getGroup(), player.getUniqueId(),
					PermissionType.getPermission("SNITCH_NOTIFICATIONS"))) {
				RateLimiter.sendMessage(player, message);
			}
		}
	}

	public static boolean immuneToSnitch(Snitch snitch, UUID accountId) {

		Group group = snitch.getGroup();
		if (group == null) {
			return true;
		}
		// Group object might be outdated so use name
		return NameAPI.getGroupManager().hasAccess(group.getName(), accountId,
			PermissionType.getPermission("SNITCH_IMMUNE"));
	}

	public static Snitch getSnitchUnderCursor(Player player) {

		SnitchManager manager = JukeAlert.getInstance().getSnitchManager();
		Iterator<Block> itr = new BlockIterator(player, 40); // Within 2.5 chunks
		while (itr.hasNext()) {
			final Block block = itr.next();
			final Material mat = block.getType();
			if (mat != Material.JUKEBOX) {
				continue;
			}
			final Snitch found = manager.getSnitch(block.getWorld(), block.getLocation());
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	public static boolean doesSnitchExist(Snitch snitch, boolean shouldCleanup) {

		Location loc = snitch.getLoc();
		World world = loc.getWorld();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		Material type = world.getBlockAt(x, y, z).getType();
		boolean exists = Material.NOTE_BLOCK == type || Material.JUKEBOX == type;
		if (!exists && shouldCleanup) {
			final JukeAlert plugin = JukeAlert.getInstance();
			plugin.log("Removing ghost snitch '" + snitch.getName() + "' at x:" + x + " y:" + y + " z:" + z);
			plugin.getSnitchManager().removeSnitch(snitch);
			plugin.getJaLogger().logSnitchBreak(world.getName(), x, y, z);
		}
		return exists;
	}

	public static Snitch findClosestSnitch(Location loc, PermissionType perm, UUID player) {

		Snitch closestSnitch = null;
		double closestDistance = Double.MAX_VALUE;
		Set<Snitch> snitches = JukeAlert.getInstance().getSnitchManager().findSnitches(loc.getWorld(), loc);
		for (final Snitch snitch : snitches) {
			if (doesSnitchExist(snitch, true) && NameAPI.getGroupManager().hasAccess(snitch.getGroup(), player, perm)) {
				double distance = snitch.getLoc().distanceSquared(loc);
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
		if (cursorSnitch != null
				&& doesSnitchExist(cursorSnitch, true)
				&& NameAPI.getGroupManager().hasAccess(cursorSnitch.getGroup(), player.getUniqueId(), perm)) {
			return cursorSnitch;
		}
		return findClosestSnitch(player.getLocation(), perm, player.getUniqueId());
	}
}
