package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.scaling.ProductionRecipeModifier;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

/**
 * Consumes a set of materials from a container and outputs another set of
 * materials to the same container
 *
 */
public class ProductionRecipe extends InputRecipe {

	private ItemMap output;
	private ItemStack recipeRepresentation;
	private ProductionRecipeModifier modifier;
	private Random rng;
	private DecimalFormat decimalFormatting;

	public ProductionRecipe(
			String identifier,
			String name,
			int productionTime,
			ItemMap inputs,
			ItemMap output,
			ItemStack recipeRepresentation,
			ProductionRecipeModifier modifier
	) {
		super(identifier, name, productionTime, inputs);
		this.output = output;
		this.modifier = modifier;
		this.rng = new Random();
		this.decimalFormatting = new DecimalFormat("#.#####");
		this.recipeRepresentation = recipeRepresentation != null ? recipeRepresentation : new ItemStack(Material.STONE);
	}

	public ItemMap getOutput() {
		return output;
	}

	public ItemMap getAdjustedOutput(int rank, int runs) {
		ItemMap im = output.clone();
		if (modifier != null) {
			im.multiplyContent(modifier.getFactor(rank, runs));
			return im;
		}
		return im;
	}

	public ItemMap getGuaranteedOutput(int rank, int runs) {
		if (modifier == null) {
			return output.clone();
		}
		ItemMap adjusted = new ItemMap();
		double factor = modifier.getFactor(rank, runs);
		for(Entry<ItemStack, Integer> entry : output.getEntrySet()) {
			adjusted.addItemAmount(entry.getKey(), (int) (Math.floor(entry.getValue() * factor)));
		}
		return adjusted;
	}

	public int getCurrentMultiplier(Inventory i) {
		return input.getMultiplesContainedIn(i);
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null || fccf == null) {
			return output.getItemStackRepresentation();
		}
		ItemMap currentOut = getGuaranteedOutput(fccf.getRecipeLevel(this), fccf.getRunCount(this));
		List<ItemStack> stacks = currentOut.getItemStackRepresentation();
		double factor = (modifier != null) ? (modifier.getFactor(fccf.getRecipeLevel(this), fccf.getRunCount(this))) : 1.0;
		for(Entry<ItemStack, Integer> entry : output.getEntrySet()) {
			double additionalChance = (((double) entry.getValue()) * factor) - currentOut.getAmount(entry.getKey());
			if (Math.abs(additionalChance) > 0.00000001) {
				ItemStack is = entry.getKey().clone();
				ItemUtils.addLore(is, ChatColor.GOLD + decimalFormatting.format(additionalChance) + " chance for additional item");
				stacks.add(is);
			}
		}
		int possibleRuns = input.getMultiplesContainedIn(i);
		for (ItemStack is : stacks) {
			ItemUtils.addLore(is, ChatColor.GREEN + "Enough materials for " + String.valueOf(possibleRuns) + " runs");
		}
		return stacks;
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		return createLoredStacksForInfo(i);
	}

	public List<ItemStack> getGuaranteedOutput(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		return createLoredStacksForInfo(i);
	}

	@Override
	public EffectFeasibility evaluateEffectFeasibility(Inventory inputInv, Inventory outputInv) {
		boolean isFeasible = input.fitsIn(outputInv);
		String reasonSnippet = isFeasible ? null : "it ran out of storage space";
		return new EffectFeasibility(
				isFeasible,
				reasonSnippet
		);
	}

	@Override
	public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
		MultiInventoryWrapper combo = new MultiInventoryWrapper(inputInv, outputInv);
		logBeforeRecipeRun(combo, fccf);
		ItemMap toRemove = input.clone();
		ItemMap toAdd;
		if (getModifier() == null) {
			toAdd = output.clone();
		}
		else {
			toAdd = getGuaranteedOutput(fccf.getRecipeLevel(this), fccf.getRunCount(this));
			double factor = modifier.getFactor(fccf.getRecipeLevel(this), fccf.getRunCount(this));
			for(Entry<ItemStack, Integer> entry : output.getEntrySet()) {
				double additionalChance = (((double) entry.getValue()) * factor) - toAdd.getAmount(entry.getKey());
				if (rng.nextDouble() <= additionalChance) {
					toAdd.addItemAmount(entry.getKey(), 1);
				}
			}
		}
		if (toRemove.isContainedIn(inputInv)) {
			if (!toAdd.fitsIn(outputInv)) { // does not fit in chest
				return false;
			}
			if (toRemove.removeSafelyFrom(inputInv)) {
				for (ItemStack is : toAdd.getItemStackRepresentation()) {
					outputInv.addItem(is);
				}
			}
		}
		logAfterRecipeRun(combo, fccf);
		return true;
	}
	
	@Override
	public Material getRecipeRepresentationMaterial() {
		return this.recipeRepresentation.getType();
	}

	public ProductionRecipeModifier getModifier() {
		return modifier;
	}

	@Override
	public String getTypeIdentifier() {
		return "PRODUCTION";
	}
	
	@Override
	public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return formatLore(output);
	}
}
