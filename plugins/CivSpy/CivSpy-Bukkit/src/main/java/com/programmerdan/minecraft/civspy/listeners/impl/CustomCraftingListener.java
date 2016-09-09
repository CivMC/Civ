package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

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
public final class CustomCraftingListener extends ServerDataListener {

	public CustomCraftingListener(DataManager target, Logger logger, String server) {
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
	 * For RecipeManager stuff gens <code>player.craft.custom</code> with recipe name as string value.
	 * 
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void CraftListen(RecipeManagerCraftEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		UUID id = player.getUniqueId();

		Location location = player.getLocation();
		Chunk chunk = location.getChunk();
		
		WorkbenchRecipe recipe = event.getRecipe();
		ItemResult result = event.getResult();
		
		ItemStack stack = result.toItemStack();
		stack.setAmount(1);
		
		DataSample recipeGen = new PointDataSample("player.craft", this.getServer(),
				chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), stack.toString(), result.getAmount());
		this.record(recipeGen);
		
		DataSample customCraft = new PointDataSample("player.craft.custom", this.getServer(),
				chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), recipe.getName());
		this.record(customCraft);
	}
}