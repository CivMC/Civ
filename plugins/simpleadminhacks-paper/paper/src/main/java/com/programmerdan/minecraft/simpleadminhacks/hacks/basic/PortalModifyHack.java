package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PortalModifyHack extends BasicHack {

	@AutoLoad
	private String targetWorld;
	@AutoLoad
	private String homeWorld;
	
	public PortalModifyHack(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}

	//We want to go last incase any plugins want to cancel our attempt
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerEnterPortal(PlayerPortalEvent event) {
		Player player = event.getPlayer();
		if (event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) {
			return;
		}
		World world = Bukkit.getWorld(targetWorld);
		if (world == null) {
			return;
		}
		Location to;
		switch (getTargetWorld(player).getEnvironment()) {
			case NETHER:
				to = new Location(getTargetWorld(player), event.getFrom().getX(), 125, event.getFrom().getZ());
				break;
			case NORMAL:
				to = getTargetWorld(player).getHighestBlockAt(player.getLocation(), HeightMap.WORLD_SURFACE).getLocation().toCenterLocation().add(0,1,0);
				break;
			default:
				return;
		}
		event.setTo(to);
		if (to.getWorld().getName().equals(targetWorld)) {
			spawnExit(to);
		}
	}

	private World getTargetWorld(Player player) {
		World target = Bukkit.getWorld(targetWorld);
		World home = Bukkit.getWorld(homeWorld);
		if (target == null) {
			return player.getWorld();
		}
		if (home == null) {
			return player.getWorld();
		}
		return player.getWorld().equals(target) ? home : target;
	}

	private void spawnExit(Location location) {
		if (location == null) {
			return;
		}
		//:Glad:
		if (location.getBlock().getRelative(0,-2,0).getType() == Material.BEDROCK) {
			location.getBlock().getRelative(0,-2,0).setType(Material.NETHERRACK);
		};
		if (location.getBlock().getRelative(0,-1,0).getType() == Material.BEDROCK) {
			location.getBlock().getRelative(0,-1,0).setType(Material.NETHERRACK);
		}
		location.getBlock().setType(Material.AIR);
		location.getBlock().getRelative(0,1,0).setType(Material.AIR);
		location.getBlock().getRelative(0,2,0).setType(Material.END_PORTAL);
	}
}
