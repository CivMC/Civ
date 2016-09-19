package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;
import com.programmerdan.minecraft.civspy.util.ItemStackToString;

/**
 * Contributes <code>player.pickup</code> stats when a person picks up something
 * <br><br>
 * Contributes <code>inventory.pickup.TYPE</code> when an inventory holder picks up an item.
 * TYPE is the Hopper or HopperMinecart that picked up the item.
 * 
 * @author ProgrammerDan
 *
 */
public class PickupListener extends ServerDataListener {

	public PickupListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}

	@Override
	public void shutdown() {
		// no-op
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void itemDropListener(PlayerPickupItemEvent event) {
		Player picker = event.getPlayer();
		if (picker == null) return;
		UUID id = picker.getUniqueId();
		Item toPick = event.getItem();
		if (toPick == null) return;
		
		Location location = toPick.getLocation();
		if (location == null) return;
		Chunk chunk = location.getChunk();
		
		ItemStack pick = toPick.getItemStack();
		if (pick == null) return;
		ItemStack pickQ = pick.clone();
		pickQ.setAmount(1);
		DataSample rpick = new PointDataSample("player.pickup", this.getServer(),
				chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), 
				ItemStackToString.toString(pickQ), pick.getAmount());
		this.record(rpick);
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void inventoryPickupListener(InventoryPickupItemEvent event) {
		Inventory inventory = event.getInventory();
		if (inventory == null) return;
		Location location = inventory.getLocation();
		Chunk chunk = location.getChunk();
		
		InventoryHolder holder = inventory.getHolder();
		if (holder == null) return;
		String picker = null;
		
		if (holder instanceof Hopper) {
			picker = "Hopper";
		} else if (holder instanceof HopperMinecart) {
			picker = "HopperMinecart";
		} else {
			picker = "Unknown";
		}
		
		Item item = event.getItem();
		ItemStack pick = item.getItemStack();
		ItemStack pickQ = pick.clone();
		pickQ.setAmount(1);
		DataSample rpick = new PointDataSample("inventory.pickup." + picker, this.getServer(),
				chunk.getWorld().getName(), null, chunk.getX(), chunk.getZ(), 
				ItemStackToString.toString(pickQ), pick.getAmount());
		this.record(rpick);
	}
}
