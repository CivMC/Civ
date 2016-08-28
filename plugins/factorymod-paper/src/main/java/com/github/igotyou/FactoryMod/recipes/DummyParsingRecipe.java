package com.github.igotyou.FactoryMod.recipes;

import java.util.List;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.factories.Factory;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class DummyParsingRecipe extends InputRecipe {

	public DummyParsingRecipe(String identifier, String name, int productionTime, ItemMap input) {
		super(identifier, name, productionTime, input);
	}

	@Override
	public void applyEffect(Inventory i, Factory f) {
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i) {
		return null;
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i) {
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
