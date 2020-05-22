package vg.civcraft.mc.civmodcore.inventorygui.paged;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import vg.civcraft.mc.civmodcore.api.EntityAPI;
import vg.civcraft.mc.civmodcore.api.InventoryAPI;

/**
 *
 */
public final class PagedGUIManager implements Listener {

	static final Map<Inventory, PagedGUI> GUIs = new HashMap<>();

	/**
	 *
	 */
	public static void closeAllGUIs() {
		for (Map.Entry<Inventory, PagedGUI> entry : GUIs.entrySet()) {
			for (Player player : InventoryAPI.getViewingPlayers(entry.getKey())) {
				player.closeInventory();
			}
			entry.getValue().reset();
		}
		GUIs.clear();
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		GUIs.computeIfPresent(event.getInventory(), (k, gui) -> gui.reset() ? null : gui);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!EntityAPI.isPlayer(event.getWhoClicked())) {
			return;
		}
		Player viewer = (Player) event.getWhoClicked();
		PagedGUI gui = GUIs.get(event.getInventory());
		if (gui == null) {
			return;
		}
		switch (event.getAction()) {
			// Disable unknown actions
			default:
			case UNKNOWN:
			// These events are cursed. There's no way to know where the items are moving to or from, just cancel.
			case COLLECT_TO_CURSOR:
			case MOVE_TO_OTHER_INVENTORY:
				event.setCancelled(true);
				return;
			// Leave these be as they aren't dangerous. Cloning a stack is an OP feature and clones to your cursor
			case NOTHING:
			case CLONE_STACK:
				return;
			// Allow the following in context to the player's inventory, but not when interacting with the GUI
			case PICKUP_ONE:
			case PICKUP_SOME:
			case PICKUP_HALF:
			case PICKUP_ALL:
			case PLACE_ONE:
			case PLACE_SOME:
			case PLACE_ALL:
			case SWAP_WITH_CURSOR:
			case DROP_ONE_SLOT:
			case DROP_ALL_SLOT:
			case DROP_ONE_CURSOR:
			case DROP_ALL_CURSOR:
			case HOTBAR_MOVE_AND_READD:
			case HOTBAR_SWAP: {
				if (event.getClickedInventory() == event.getInventory()) {
					event.setCancelled(true);
					gui.clicked(event.getSlot(), viewer, event.getClick());
				}
			}
		}
	}

}
