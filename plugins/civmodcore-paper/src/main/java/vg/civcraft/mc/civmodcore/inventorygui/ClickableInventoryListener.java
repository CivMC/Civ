package vg.civcraft.mc.civmodcore.inventorygui;

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
	public void inventoryClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player)) {
			return;
		}
		Player p = (Player) e.getWhoClicked();
		ClickableInventory ci = ClickableInventory.getOpenInventory(p);
		if (ci != null) {
			e.setCancelled(true); // always cancel first to prevent dupes
			ci.itemClick(p, e.getRawSlot());
		}
	}

	@EventHandler
	public void inventoryClose(InventoryCloseEvent e) {
		// for some reason getPlayer apparently isnt always a player here, but
		// just a HumanEntity
		if (!(e.getPlayer() instanceof Player)) {
			return;
		}
		Player p = (Player) e.getPlayer();
		ClickableInventory.inventoryWasClosed(p);
	}

	@EventHandler
	public void playerLogoff(PlayerQuitEvent e) {
		// this just does nothing if no inventory was open
		ClickableInventory.inventoryWasClosed(e.getPlayer());
	}

}
