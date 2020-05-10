package com.github.igotyou.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.ItemNames;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class PylonRecipe extends InputRecipe {

	private ItemMap output;
	private static int currentGlobalWeight;
	private static int globalLimit;
	private int weight;

	public PylonRecipe(String identifier, String name, int productionTime, ItemMap input,
			ItemMap output, int weight) {
		super(identifier, name, productionTime, input);
		this.output = output;
		this.weight = weight;
	}

	@Override
	public boolean applyEffect(Inventory i, FurnCraftChestFactory fccf) {
		if (!input.isContainedIn(i)) {
			return false;
		}
		ItemMap actualOutput = getCurrentOutput();
		if (!actualOutput.fitsIn(i)) {
			return false;
		}
		if (input.removeSafelyFrom(i)) {
			for (ItemStack is : actualOutput.getItemStackRepresentation()) {
				i.addItem(is);
			}
		}
		return true;
	}

	public static void setGlobalLimit(int limit) {
		globalLimit = limit;
	}

	public static int getGlobalLimit() {
		return globalLimit;
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		ItemMap currOut = getCurrentOutput();
		List<ItemStack> res = new LinkedList<ItemStack>();
		for (ItemStack is : currOut.getItemStackRepresentation()) {
			ItemAPI.setLore(is, ChatColor.GOLD + "Currently there are "
					+ FurnCraftChestFactory.getPylonFactories() == null ? "0"
					: FurnCraftChestFactory.getPylonFactories().size()
							+ " pylons on the map", ChatColor.RED
					+ "Current global weight is " + currentGlobalWeight);
			res.add(is);
		}
		return res;
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		return createLoredStacksForInfo(i);
	}
	
	@Override
	public Material getRecipeRepresentationMaterial() {
		return output.getItemStackRepresentation().get(0).getType();
	}

	@Override
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
		int weight = 0;
		Set<FurnCraftChestFactory> pylons = FurnCraftChestFactory
				.getPylonFactories();
		if (pylons != null) {
			// if not a single factory (not limited to pylon) is in the map,
			// this will be null
			for (FurnCraftChestFactory f : pylons) {
				if (f.isActive() && f.getCurrentRecipe() instanceof PylonRecipe) {
					weight += ((PylonRecipe) f.getCurrentRecipe()).getWeight();
				}
			}
		}
		currentGlobalWeight = weight;
		double overload = Math.max(1.0, (float) currentGlobalWeight
				/ (float) globalLimit);
		double multiplier = 1.0 / overload;
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

	@Override
	public String getTypeIdentifier() {
		return "PYLON";
	}

	public ItemMap getOutput() {
		return output;
	}

	@Override
	public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List<String> result = new ArrayList<>();
		for(Entry <ItemStack, Integer> entry : output.getEntrySet()) {
			if (entry.getValue() > 0) {
				result.add(entry.getValue() + " " + ItemNames.getItemName(entry.getKey()));
			}
		}
		return result;
	}
}
