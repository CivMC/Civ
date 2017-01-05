package vg.civcraft.mc.citadel.listener;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.namelayer.group.Group;

public class InventoryListener implements Listener {
	private ReinforcementManager rm = Citadel.getReinforcementManager();

	public PlayerReinforcement getReinforcement(Inventory inv) {
		// Returns reinforcement of the inventory's holder or null if none
		// exists
		final InventoryHolder holder = inv.getHolder();
		Location loc;
		if (holder instanceof DoubleChest) {
			loc = Utility.getRealBlock(((DoubleChest) holder).getLocation().getBlock()).getLocation();
		} else if (holder instanceof BlockState) {
			loc = Utility.getRealBlock(((BlockState) holder).getLocation().getBlock()).getLocation();
		} else {
			// Entity or Vehicle inventories
			return null;
		}
		Reinforcement rein = rm.getReinforcement(loc);
		if (rein instanceof PlayerReinforcement) {
			PlayerReinforcement pr = (PlayerReinforcement) rein;
			return pr;
		}
		return null;
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		// Prevent hopper minecarts from extracting from reinforced containers
		// or
		// filling up reinforced containers.
		// Prevent misowned hoppers from stealing from reinforced containers.
		//
		// Allowed transfers:
		// Assertions:
		// Public reinforcement == Non-reinforced
		// Y is a member of Group X
		// Insecure sources act like public reinforcements
		// Public -> Public
		// Public -> Group X
		// Public -> Personal Y
		// Group X -> Group X
		// Group X -> Personal Y
		// Personal Y -> Personal Y

		// Fail safe
		event.setCancelled(true);
		// Source
		final Inventory src = event.getSource();
		final PlayerReinforcement srcRein = getReinforcement(src);
		// Destination
		final Inventory dest = event.getDestination();
		final PlayerReinforcement destRein = getReinforcement(dest);
		if (srcRein == null) {
			if (destRein != null) {
				final Group destOwner = destRein.getGroup();
				if (destOwner != null && destOwner.isDisciplined()) {
					// Dest is disabled, deny
					return;
				}
			}
			// Public can transfer into any, allow
			// (Public -> Public, Public -> Group X, Public -> Personal Y)
			event.setCancelled(false);
			return;
		}
		if (srcRein.isInsecure()) {
			// Insecure source reinforcement allows transfer as if it's
			// a public reinforcement, allow
			event.setCancelled(false);
			return;
		}
		// Assertion: srcRein != null
		if (destRein == null) {
			// Non-public cannot transfer into a public, deny
			return;
		}
		// Assertion: srcRein != null && destRein != null
		final Group srcOwner = srcRein.getGroup();
		final Group destOwner = destRein.getGroup();
		// Check for null group failure
		if (srcOwner == null || destOwner == null) {
			/*
			 * if (Citadel.verboseEnabled(VerboseMsg.NullGroup)) { String msg;
			 * if (srcOwner == null) { msg = Citadel.verboseFmt(
			 * VerboseMsg.NullGroup, "srcOwner", srcRein.getOwnerName()); } else
			 * { // destOwner == null msg = Citadel.verboseFmt(
			 * VerboseMsg.NullGroup, "dstOwner", destRein.getOwnerName()); } if
			 * (!priorMessages_.contains(msg)) { Citadel.info(msg);
			 * priorMessages_.add(msg); } }
			 */
			// Unable to determine reinforcement owner match, deny
			return;
		}
		if (srcOwner.isDisciplined() || destOwner.isDisciplined()) {
			// Disabled group involved, deny
			return;
		}
		if (srcOwner == destOwner) {
			// Reinforcement owners match, allow
			// (Group X -> Group X, Personal Y -> Personal Y)
			event.setCancelled(false);
			return;
		}
		// Reinforcement owners don't match, deny
	}
}