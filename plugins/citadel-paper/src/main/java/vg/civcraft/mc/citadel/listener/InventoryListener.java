package vg.civcraft.mc.citadel.listener;

import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;

import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;

public class InventoryListener implements Listener {

	// prevent sucking reinforcements out of reinforced containers with hoppers on
	// different groups or filling into them
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		Inventory sourceInv = event.getSource();
		Reinforcement sourceRein = null;
		if (sourceInv.getHolder() instanceof Container) {
			sourceRein = ReinforcementLogic.getReinforcementProtecting(((Container) sourceInv.getHolder()).getBlock());
		}
		Inventory destInv = event.getDestination();
		Reinforcement destRein = null;
		if (destInv.getHolder() instanceof Container) {
			destRein = ReinforcementLogic.getReinforcementProtecting(((Container) destInv.getHolder()).getBlock());
		}
		if (sourceRein == null && destRein == null) {
			return;
		}
		if (sourceRein != null && destRein == null) {
			if (!sourceRein.isInsecure()) {
				event.setCancelled(true);
			}
			return;
		}
		if (sourceRein == null) {
			if (!destRein.isInsecure()) {
				event.setCancelled(true);
			}
			return;
		}
		// both reinforced at this point
		if (sourceRein.getGroup().getGroupId() != destRein.getGroup().getGroupId()) {
			event.setCancelled(true);
		}
	}
}