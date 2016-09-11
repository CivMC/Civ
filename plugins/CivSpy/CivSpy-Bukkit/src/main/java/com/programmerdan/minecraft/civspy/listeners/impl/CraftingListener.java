package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.CraftItemEvent;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;
import com.programmerdan.minecraft.civspy.util.ItemStackToString;

/**
 * Listener that records crafting recipe use.
 * 
 * @author ProgrammerDan
 */
public final class CraftingListener extends ServerDataListener {

	public CraftingListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}
	
	@Override
	public void shutdown() {
		// NO-OP
	}
	
	/**
	 * Generates: <code>player.craft</code> stat_key data.
	 * ItemStack size stored in number value, serialized string in string value.
	 * 
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void CraftListen(CraftItemEvent event) {
		HumanEntity player = event.getWhoClicked();
		if (player == null) return;
		UUID id = player.getUniqueId();

		Location location = player.getLocation();
		Chunk chunk = location.getChunk();
		
		CraftingInventory resultMap = event.getInventory();
		ItemStack result = resultMap.getResult();
		
		ItemStack stack = result.clone();
		stack.setAmount(1);
		
		DataSample recipeGen = new PointDataSample("player.craft", this.getServer(),
				chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), 
				ItemStackToString.toString(stack), result.getAmount());
		this.record(recipeGen);
	}
}