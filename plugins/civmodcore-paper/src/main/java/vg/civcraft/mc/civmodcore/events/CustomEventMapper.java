package vg.civcraft.mc.civmodcore.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public class CustomEventMapper implements Listener {

	/**
	 * Glue map for {@link PlayerMoveBlockEvent}.
	 * @param event The event to map from.
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void detectPlayerMoveBlock(PlayerMoveEvent event) {
		Location formerLocation = event.getFrom();
		Location latterLocation = event.getTo();
		// If no block movement has occurred, exit out
		if (!WorldUtils.doLocationsHaveSameWorld(formerLocation, latterLocation)
				|| formerLocation.getBlockX() != latterLocation.getBlockX()
				|| formerLocation.getBlockY() != latterLocation.getBlockY()
				|| formerLocation.getBlockZ() != latterLocation.getBlockZ()) {
			return;
		}
		PlayerMoveBlockEvent better = new PlayerMoveBlockEvent(event.getPlayer(), formerLocation, latterLocation);
		Bukkit.getPluginManager().callEvent(better);
		event.setCancelled(better.isCancelled());
	}

}
