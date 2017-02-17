package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;
import com.programmerdan.minecraft.civspy.util.ItemStackToString;

/**
 * Contributes <code>inventory.moveto.TYPE</code> stats when items move to an inventory, paired with
 * Contributes <code>inventory.movefrom.TYPE</code> stats when items leaves an inventory.
 * String value is the item moved, Numeric Value is how many.
 * 
 * Contributes <code>player.INV.slot.ACTION</code> data when a player interacts with an inventory slot.
 * Contributes <code>player.INV.cursor.ACTION</code> data when a player interacts with their cursor.
 * INV is source inventory type; ACTION is the nature of the interaction.
 * String value is the item moved, Numeric Value is how many.
 * 
 * Contributes <code>player.open</code> when a player opens an inventory holder.
 * String value is the TYPE of inventory opened.
 * 
 * @author ProgrammerDan
 *
 */
public class InventoryListener extends ServerDataListener {

	public InventoryListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}

	@Override
	public void shutdown() {
		// no-op
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void inventoryOpenListener(InventoryOpenEvent event) {
		try {
			HumanEntity picker = event.getPlayer();
			if (picker == null) return;
			UUID id = picker.getUniqueId();
			
			Inventory opened = event.getInventory();
			if (opened == null) return;
			Location location = opened.getLocation();
			if (location == null) return;
			Chunk chunk = location.getChunk();
			String type = resolveType(opened);
			
			DataSample iopen = new PointDataSample("player.open", this.getServer(),
					chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), 
					type );
			this.record(iopen);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy a player inventory open event", e);
		}
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void inventoryCloseListener(InventoryCloseEvent event) {
		try {
			HumanEntity picker = event.getPlayer();
			if (picker == null) return;
			UUID id = picker.getUniqueId();
			
			Inventory opened = event.getInventory();
			if (opened == null) return;
			Location location = opened.getLocation();
			if (location == null) return;
			Chunk chunk = location.getChunk();
			String type = resolveType(opened);
			
			DataSample iclose = new PointDataSample("player.close", this.getServer(),
					chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), 
					type );
			this.record(iclose);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy a player inventory close event", e);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void inventoryMoveListener(InventoryMoveItemEvent event) {
		try {
			Inventory source = event.getSource();
			if (source == null) return;
			Location sourceLocation = source.getLocation();
			Chunk sourceChunk = sourceLocation.getChunk();
			String sourcePicker = resolveType(source);

			Inventory destination = event.getDestination();
			if (destination == null) return;
			Location destinationLocation = destination.getLocation();
			Chunk destinationChunk = destinationLocation.getChunk();
			String destinationPicker = resolveType(destination);

			ItemStack pick = event.getItem();
			if (pick == null) return;
			ItemStack pickQ = pick.clone();
			pickQ.setAmount(1);

			DataSample moveto = new PointDataSample("inventory.moveto." + destinationPicker, this.getServer(),
					destinationChunk.getWorld().getName(), null, destinationChunk.getX(), destinationChunk.getZ(), 
					ItemStackToString.toString(pickQ), pick.getAmount());
			this.record(moveto);

			DataSample movefrom = new PointDataSample("inventory.movefrom." + sourcePicker, this.getServer(),
					sourceChunk.getWorld().getName(), null, sourceChunk.getX(), sourceChunk.getZ(), 
					ItemStackToString.toString(pickQ), pick.getAmount());
			this.record(movefrom);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy an inventory move event", e);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void inventoryClickListener(InventoryClickEvent event) {
		try {
			if (Event.Result.DENY.equals(event.getResult())) return;
			
			// Big picture, I want to know the follow:
			// Pick up from
			// Place to
			// or, for swap, both.
			// What was Manip'd
			// How much was Manip'd
			// Who was involved.
			
			Location location = null;
			Chunk chunk = null;
			
			Inventory source = event.getClickedInventory();
			String sourcePicker = "NONE";
			if (source != null) {
				location = source.getLocation();
				sourcePicker = resolveType(source);
			}
			
			HumanEntity picker = event.getWhoClicked();
			if (picker == null) return;
			if (location == null) {
				location = picker.getLocation();
			}
			chunk = location.getChunk();
			UUID id = picker.getUniqueId();

			ItemStack inHand = event.getCursor();
			ItemStack inInv = event.getCurrentItem();
			
			boolean doCursor = false;
			boolean doSlot = false;
			String cursorAction = null;
			String slotAction = null;
			ItemStack cursorItem = null;
			ItemStack slotItem = null;
			
			switch(event.getAction()) {
			case CLONE_STACK:
				// creative?
				if (!picker.isOp()) {
					logger.log(Level.WARNING, "Detected action clone from non-op!");
				}
				return;
				
			case COLLECT_TO_CURSOR:
				// When you doubleclick on an item in the crafting grid for example.
				slotAction = "FROM_INV";
				slotItem = (inInv != null) ? inInv : inHand;
				doSlot = true;
				break;
				
			case DROP_ALL_CURSOR: // press DROP while holding something
			case DROP_ONE_CURSOR:
				cursorAction = "DROP_CURSOR";
				cursorItem = inHand;
				doCursor = true;
				break;
				
			case DROP_ALL_SLOT:
			case DROP_ONE_SLOT: // press DROP over a slot
				slotAction = "DROP_INV";
				slotItem = inInv;
				doSlot = true;
				break;
			
			case MOVE_TO_OTHER_INVENTORY:
				// SHIFT click swap to other inventory
				slotAction = "MOVE_INV";
				slotItem = inInv;
				doSlot = true;
				break;

			case PICKUP_ALL:
			case PICKUP_HALF:
			case PICKUP_ONE:
			case PICKUP_SOME:
				// Pickup FROM container TO cursor
				slotAction = "TO_CURSOR";
				slotItem = inInv;
				doSlot = true;
				break;

			case PLACE_ALL:
			case PLACE_ONE:
			case PLACE_SOME:
				// Place FROM cursor TO container
				cursorAction = "FROM_CURSOR";
				cursorItem = inHand;
				doCursor = true;
				break;

			case HOTBAR_MOVE_AND_READD: //?? I think, move from one part of inventory to another
			case HOTBAR_SWAP: // SHIFT swap from inventory to / from hotbar
				slotAction = "SWAP_INV_OUT";
				slotItem = inInv;
				doSlot = true;
				cursorAction = "SWAP_INV_IN";
				cursorItem = inHand;
				doCursor = true;
				// ???
				break;
			case SWAP_WITH_CURSOR: // click while you have something already on the cursor (swap)
				slotAction = "SWAP_CURSOR_OUT";
				slotItem = inInv;
				doSlot = true;
				cursorAction = "SWAP_CURSOR_IN";
				cursorItem = inHand;
				doCursor = true;
				break;
			case NOTHING:
			case UNKNOWN:
			default:
				logger.log(Level.FINE, "Detected unknown inventory action!");
				slotAction = "UNKNOWN_SLOT";
				slotItem = inInv;
				doSlot = (inInv != null && Material.AIR.equals(inInv.getType()));
				cursorAction = "UNKNOWN_CURSOR";
				cursorItem = inHand;
				doCursor = (inHand != null && Material.AIR.equals(inHand.getType()));
				break;
			}

			if (doSlot) {
				//logger.log(Level.FINER, "Slot Action {0} holds {1}", new Object[]{slotAction, ItemStackToString.toString(slotItem)});
				ItemStack cloneItem = slotItem.clone();
				cloneItem.setAmount(1);
				DataSample slotData = new PointDataSample("player.slot." + sourcePicker + "." + slotAction, this.getServer(),
						chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(),
						ItemStackToString.toString(cloneItem), slotItem.getAmount());
				this.record(slotData);
			}
			
			if (doCursor) {
				//logger.log(Level.FINER, "Cursor Action {0} holds {1}", new Object[]{cursorAction, ItemStackToString.toString(cursorItem)});
				ItemStack cloneItem = cursorItem.clone();
				cloneItem.setAmount(1);
				DataSample cursorData = new PointDataSample("player.cursor." + sourcePicker + "." + cursorAction, this.getServer(),
						chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(),
						ItemStackToString.toString(cloneItem), cursorItem.getAmount());
				this.record(cursorData);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy an inventory click event", e);
		}
	}
	
	private static String resolveType(Inventory holder) {
		InventoryType type = holder.getType();
		if (type == null) {
			return "Unknown";
		} else {
			return type.getDefaultTitle();
		}
	}
}
