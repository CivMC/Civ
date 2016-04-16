package com.github.igotyou.FactoryMod.events;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;

import vg.civcraft.mc.civmodcore.interfaces.CustomEvent;

/**
 * Event called when executing a recipe in a FurnCraftChestFactory
 */
public class RecipeExecuteEvent extends CustomEvent {
	private FurnCraftChestFactory fccf;
	private InputRecipe rec;
	
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

}
