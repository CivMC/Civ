package vg.civcraft.mc.civmodcore.inventory.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * The listener which makes ClickableInventories work. To use this either register it as a listener in your plugin or
 * extend your plugin class from ACivMod, DONT DO BOTH
 *
 * @author Maxopoly
 */
public class ClickableInventoryListener implements Listener {

	@EventHandler
	public void inventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player player)) {
			return;
		}
		ClickableInventory inventory = ClickableInventory.getOpenInventory(player);
		if (inventory != null) {
			event.setCancelled(true); // always cancel first to prevent dupes
			inventory.itemClick(player, event.getRawSlot(), event.getClick());
		}
	}

	@EventHandler
	public void inventoryClose(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player player)) { // Despite the name, it's not necessarily a player
			return;
		}
		ClickableInventory.inventoryWasClosed(player);
	}

	@EventHandler
	public void playerLogoff(PlayerQuitEvent event) {
		ClickableInventory.inventoryWasClosed(event.getPlayer());
	}

}
