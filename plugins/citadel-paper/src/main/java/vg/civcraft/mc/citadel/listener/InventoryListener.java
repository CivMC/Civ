package vg.civcraft.mc.citadel.listener;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.Dispenser;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Dropper;
import org.bukkit.block.Hopper;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.api.BlockAPI;
import vg.civcraft.mc.civmodcore.api.WorldAPI;
import vg.civcraft.mc.civmodcore.util.Iteration;

public class InventoryListener implements Listener {

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		Inventory fromInventory = event.getSource();
		Inventory destInventory = event.getDestination();
		// [LagFix] If either inventory's base location is unloaded, just cancel
		if (!WorldAPI.isBlockLoaded(fromInventory.getLocation())
				|| !WorldAPI.isBlockLoaded(destInventory.getLocation())) {
			event.setCancelled(true);
			return;
		}
		InventoryHolder fromHolder = fromInventory.getHolder();
		InventoryHolder destHolder = destInventory.getHolder();
		// Determine the reinforcement of the source
		Reinforcement fromReinforcement = null;
		if (fromHolder instanceof DoubleChest) {
			DoubleChest doubleChest = (DoubleChest) fromHolder;
			Location chestLocation = Objects.requireNonNull((Chest) doubleChest.getLeftSide()).getLocation();
			Location otherLocation = Objects.requireNonNull((Chest) doubleChest.getRightSide()).getLocation();
			// [LagFix] If either side of the double chest is not loaded then the reinforcement cannot be retrieved
			// [LagFix] without necessarily loading the chunk to check against reinforcement logic, therefore this
			// [LagFix] should air on the side of caution and prevent the transfer.
			if (!WorldAPI.isBlockLoaded(chestLocation) || !WorldAPI.isBlockLoaded(otherLocation)) {
				event.setCancelled(true);
				return;
			}
			if (destHolder instanceof Hopper) {
				Location drainedLocation = ((Hopper) destHolder).getLocation().add(0, 1, 0);
				if (Iteration.contains(drainedLocation, chestLocation, otherLocation)) {
					fromReinforcement = ReinforcementLogic.getReinforcementProtecting(drainedLocation.getBlock());
				}
			}
		}
		else if (fromHolder instanceof Container) {
			Container container = (Container) fromHolder;
			// [LagFix] This shouldn't be contributing to lag since there's isn't an implementation of 'Container' that
			// [LagFix] spans more than one block, so the 'container.getBlock()' is permissible since we can reasonably
			// [LagFix] assume that since this event was called, this block is loaded.
			fromReinforcement = ReinforcementLogic.getReinforcementProtecting(container.getBlock());
		}
		// Determine the reinforcement of the destination
		Reinforcement destReinforcement = null;
		if (fromHolder instanceof Hopper || fromHolder instanceof Dropper || fromHolder instanceof Dispenser) {
			Container container = (Container) fromHolder;
			BlockFace direction = ((Directional) container.getBlockData()).getFacing();
			// [LagFix] If the transfer is happening laterally and the target location is not loaded, then air on the
			// [LagFix] side of caution and prevent the transfer.. though this may cause some issues with dispensers
			// [LagFix] and droppers.
			Location target = container.getLocation().add(direction.getDirection());
			if (BlockAPI.PLANAR_SIDES.contains(direction) && !WorldAPI.isBlockLoaded(target)) {
				event.setCancelled(true);
				return;
			}
			destReinforcement = ReinforcementLogic.getReinforcementProtecting(target.getBlock());
		}
		else if (destHolder instanceof Container) {
			Container container = (Container) destHolder;
			// [LagFix] Just like the other 'Container' this shouldn't be an issue.
			destReinforcement = ReinforcementLogic.getReinforcementProtecting(container.getBlock());
		}
		// Allow the transfer if neither are reinforced
		if (fromReinforcement == null && destReinforcement == null) {
			return;
		}
		// Allow the transfer if the destination is un-reinforced and the source is insecure
		if (destReinforcement == null) {
			if (!fromReinforcement.isInsecure()) {
				event.setCancelled(true);
			}
			return;
		}
		// Allow the transfer if the source is un-reinforced and the destination is insecure
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

}
