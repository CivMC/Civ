package vg.civcraft.mc.citadel.listener;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import vg.civcraft.mc.civmodcore.util.Iteration;

public class InventoryListener implements Listener {

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		Inventory fromInventory = event.getSource();
		Inventory destInventory = event.getDestination();
		InventoryHolder fromHolder = fromInventory.getHolder();
		InventoryHolder destHolder = destInventory.getHolder();
		// Determine the reinforcement of the source
		Reinforcement fromReinforcement = null;
		if (fromHolder instanceof Container) {
			Container container = (Container) fromHolder;
			fromReinforcement = ReinforcementLogic.getReinforcementProtecting(container.getBlock());
		}
		else if (fromHolder instanceof DoubleChest) {
			DoubleChest doubleChest = (DoubleChest) fromHolder;
			Block doubleChestBlock = doubleChest.getLocation().getBlock();
			Location chestLocation = doubleChestBlock.getLocation(); // Yes this is necessary
			Location otherLocation = BlockAPI.getOtherDoubleChestBlock(doubleChestBlock).getLocation();
			if (destHolder instanceof Hopper) {
				Location drainedLocation = ((Hopper) destHolder).getLocation().add(0, 1, 0);
				if (Iteration.contains(drainedLocation, chestLocation, otherLocation)) {
					fromReinforcement = ReinforcementLogic.getReinforcementProtecting(drainedLocation.getBlock());
				}
			}
		}
		// Determine the reinforcement of the destination
		Reinforcement destReinforcement = null;
		if (fromHolder instanceof Hopper || fromHolder instanceof Dropper || fromHolder instanceof Dispenser) {
			Container container = (Container) fromHolder;
			BlockFace direction = ((Directional) container.getBlockData()).getFacing();
			destReinforcement = ReinforcementLogic.getReinforcementProtecting(
					container.getBlock().getRelative(direction));
		}
		else if (destHolder instanceof Container) {
			Container container = (Container) destHolder;
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
