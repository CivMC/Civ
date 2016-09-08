package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import haveric.recipeManager.api.events.RecipeManagerCraftEvent;
import haveric.recipeManager.recipes.ItemResult;
import haveric.recipeManager.recipes.WorkbenchRecipe;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;

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
	 * ItemStack size stored in number value, toString in string value.
	 * 
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void CraftListen(RecipeManagerCraftEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		UUID id = player.getUniqueId();

		Location location = p.getLocation();
		Chunk chunk = location.getChunk();
		
		StringBuilder blockName = new StringBuilder(broken.getType().toString());
		blockName.append(":").append(broken.getData());

		DataSample blockBreak = new PointDataSample("player.blockbreak", this.getServer(),
				chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), blockName.toString());
		this.record(blockBreak);
	}

}

