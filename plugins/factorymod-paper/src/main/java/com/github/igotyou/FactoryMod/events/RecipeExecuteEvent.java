package com.github.igotyou.FactoryMod.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;

/**
 * Event called when executing a recipe in a FurnCraftChestFactory
 */
public class RecipeExecuteEvent extends Event implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();
	
	private FurnCraftChestFactory fccf;
	private InputRecipe rec;

	private boolean cancelled;

	public RecipeExecuteEvent(FurnCraftChestFactory fccf, InputRecipe rec) {
		this.rec = rec;
		this.fccf = fccf;
	}

	/**
	 * @return The factory executing the recipe
	 */
	public FurnCraftChestFactory getFactory() {
		return fccf;
	}

	/**
	 * @return The recipe being executed
	 */
	public InputRecipe getRecipe() {
		return rec;
	}
	

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;		
	}

}
