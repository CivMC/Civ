package com.github.igotyou.FactoryMod.recipes;

import java.util.List;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class DummyParsingRecipe extends InputRecipe {

	public DummyParsingRecipe(String identifier, String name, int productionTime, ItemMap input) {
		super(identifier, name, productionTime, input);
	}

	@Override
	public void applyEffect(Inventory i, FurnCraftChestFactory fccf) {
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return null;
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return null;
	}

	@Override
	public ItemStack getRecipeRepresentation() {
		return null;
	}

	@Override
	public String getTypeIdentifier() {
		return "DUMMY";
	}

}
