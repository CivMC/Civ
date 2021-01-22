package vg.civcraft.mc.citadel.listener;

import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public class InventoryListener implements Listener {

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		Inventory fromInventory = event.getSource();
		InventoryHolder fromHolder = fromInventory.getHolder();
		boolean isFromBlock = fromHolder instanceof Container;
		boolean fromAtChunkBorder = false;
		Location fromLocation = null;
		if (isFromBlock) {
			fromLocation = fromInventory.getLocation();
			fromAtChunkBorder = isAtChunkBorder(fromLocation);
			if (fromAtChunkBorder && !WorldUtils.isBlockLoaded(fromLocation)) {
				event.setCancelled(true);
				return;
			}
		}

		Inventory destInventory = event.getDestination();
		InventoryHolder destHolder = destInventory.getHolder();
		boolean isDestBlock = destHolder instanceof Container;
		boolean destAtChunkBorder = false;
		Location destLocation = null;
		if (isDestBlock) {
			destLocation = destInventory.getLocation();
			destAtChunkBorder = isAtChunkBorder(destLocation);
			if (destAtChunkBorder && !WorldUtils.isBlockLoaded(destLocation)) {
				event.setCancelled(true);
				return;
			}
		} else {
			if (!isFromBlock) {
				// neither is a block, just ignore entirely
				return;
			}
		}

		// Determine the reinforcement of the source
		Reinforcement fromReinforcement = null;
		if (isFromBlock) {
			if (fromAtChunkBorder && fromHolder instanceof DoubleChest) {
				DoubleChest doubleChest = (DoubleChest) fromHolder;
				Location chestLocation = ((Chest) doubleChest.getLeftSide()).getLocation();
				Location otherLocation = ((Chest) doubleChest.getRightSide()).getLocation();
				// [LagFix] If either side of the double chest is not loaded then the
				// reinforcement cannot be retrieved
				// [LagFix] without necessarily loading the chunk to check against reinforcement
				// logic, therefore this
				// [LagFix] should err on the side of caution and prevent the transfer.
				if (!WorldUtils.isBlockLoaded(chestLocation) || !WorldUtils.isBlockLoaded(otherLocation)) {
					event.setCancelled(true);
					return;
				}
			}
			fromReinforcement = ReinforcementLogic.getReinforcementProtecting(fromLocation.getBlock());
		}
		// Determine the reinforcement of the destination
		Reinforcement destReinforcement = null;
		if (isDestBlock) {
			if (destAtChunkBorder && destHolder instanceof DoubleChest) {
				DoubleChest doubleChest = (DoubleChest) destHolder;
				Location chestLocation = ((Chest) doubleChest.getLeftSide()).getLocation();
				Location otherLocation = ((Chest) doubleChest.getRightSide()).getLocation();
				// [LagFix] If either side of the double chest is not loaded then the
				// reinforcement cannot be retrieved
				// [LagFix] without necessarily loading the chunk to check against reinforcement
				// logic, therefore this
				// [LagFix] should err on the side of caution and prevent the transfer.
				if (!WorldUtils.isBlockLoaded(chestLocation) || !WorldUtils.isBlockLoaded(otherLocation)) {
					event.setCancelled(true);
					return;
				}
			}
			destReinforcement = ReinforcementLogic.getReinforcementProtecting(destLocation.getBlock());
		}
		// Allow the transfer if neither are reinforced
		if (fromReinforcement == null && destReinforcement == null) {
			return;
		}
		// Allow the transfer if the destination is un-reinforced and the source is
		// insecure
		if (destReinforcement == null) {
			if (!fromReinforcement.isInsecure()) {
				event.setCancelled(true);
			}
			return;
		}
		// Allow the transfer if the source is un-reinforced and the destination is
		// insecure
		if (fromReinforcement == null) {
			if (!destReinforcement.isInsecure()) {
				event.setCancelled(true);
			}
			return;
		}
		// Allow the transfer if both the source and destination are insecure
		if (fromReinforcement.isInsecure() && destReinforcement.isInsecure()) {
			return;
		}
		// Allow the transfer if both the source and destination are on the same group
		if (fromReinforcement.getGroupId() == destReinforcement.getGroupId()) {
			return;
		}
		event.setCancelled(true);
	}
	
	private static boolean isAtChunkBorder(Location location) {
		int xShif = location.getBlockX() & 15;
		int zShif = location.getBlockZ() & 15;
		return xShif == 0 || xShif == 15 || zShif == 0 || zShif == 15;
	}

}
