package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

public class PylonRecipe extends InputRecipe {

	private ItemMap output;
	private static int currentGlobalWeight;
	private int weight;
	private double maximumFraction;

	public PylonRecipe(String name, int productionTime, ItemMap input,
			ItemMap output, int weight) {
		super(name, productionTime, input);
		this.output = output;
		this.weight = weight;
	}

	public void applyEffect(Inventory i, Factory f) {
		if (!input.isContainedIn(i)) {
			return;
		}
		ItemMap actualOutput = getCurrentOutput();
		if (!actualOutput.fitsIn(i)) {
			return;
		}
		if (input.removeSafelyFrom(i)) {
			for (ItemStack is : actualOutput.getItemStackRepresentation()) {
				i.addItem(is);
			}
		}
	}

	public List<ItemStack> getOutputRepresentation(Inventory i) {
		ItemMap currOut = getCurrentOutput();
		List<ItemStack> res = new LinkedList<ItemStack>();
		for (ItemStack is : currOut.getItemStackRepresentation()) {
			ISUtils.setLore(is, ChatColor.GOLD + "Currently there are "
					+ FurnCraftChestFactory.getPylonFactories().size()
					+ " pylons on the map", ChatColor.RED
					+ "Current global weight is " + currentGlobalWeight);
			res.add(is);
		}
		return res;
	}

	public List<ItemStack> getInputRepresentation(Inventory i) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		return createLoredStacksForInfo(i);
	}

	public ItemStack getRecipeRepresentation() {
		List<ItemStack> out = output.getItemStackRepresentation();
		ItemStack res;
		if (out.size() == 0) {
			res = new ItemStack(Material.STONE);
		} else {
			res = out.get(0);
		}
		ISUtils.setName(res, getRecipeName());
		return res;
	}

	public boolean enoughMaterialAvailable(Inventory i) {
		return input.isContainedIn(i) && skyView();
	}

	public int getWeight() {
		return weight;
	}

	private boolean skyView() {
		return true;
		// place holder in case we want to do something with it in the future
	}

	private ItemMap getCurrentOutput() {
		double multiplier = Math.min(1 / currentGlobalWeight, maximumFraction);
		ItemMap actualOutput = new ItemMap();
		for (Entry<ItemStack, Integer> entry : output.getEntrySet()) {
			actualOutput.addItemAmount(entry.getKey(),
					(int) (entry.getValue() * multiplier));
		}
		return actualOutput;
	}

	public static void addWeight(int weight) {
		currentGlobalWeight += weight;
	}

	public static void removeWeight(int weight) {
		currentGlobalWeight -= weight;
	}

}
