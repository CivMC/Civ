package com.github.igotyou.FactoryMod.recipes;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.ItemNames;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class LoreEnchantRecipe extends InputRecipe {

	private List<String> appliedLore;
	private List<String> overwritenLore;
	private ItemMap tool;
	private ItemStack exampleInput;
	private ItemStack exampleOutput;

	public LoreEnchantRecipe(String identifier, String name, int productionTime, ItemMap input, ItemMap tool, List<String> appliedLore,
			List<String> overwritenLore) {
		super(identifier, name, productionTime, input);
		this.overwritenLore = overwritenLore;
		this.appliedLore = appliedLore;
		this.tool = tool;
		exampleInput = tool.getItemStackRepresentation().get(0);
		for (String s : overwritenLore) {
			ItemAPI.addLore(exampleInput, s);
		}
		exampleOutput = tool.getItemStackRepresentation().get(0);
		for (String s : appliedLore) {
			ItemAPI.addLore(exampleOutput, s);
		}
	}

	@Override
	public boolean enoughMaterialAvailable(Inventory i) {
		if (input.isContainedIn(i)) {
			ItemStack toolio = tool.getItemStackRepresentation().get(0);
			for (ItemStack is : i.getContents()) {
				if (is != null && toolio.getType() == is.getType() && hasStackRequiredLore(is)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Material getRecipeRepresentationMaterial() {
		return exampleOutput.getType();
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		ItemStack is = exampleOutput.clone();
		if (i != null) {
			ItemAPI.addLore(
					is,
					ChatColor.GREEN
							+ "Enough materials for "
							+ String.valueOf(Math.min(tool.getMultiplesContainedIn(i), input.getMultiplesContainedIn(i)))
							+ " runs");
		}
		List<ItemStack> stacks = new LinkedList<>();
		stacks.add(is);
		return stacks;
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			return Arrays.asList(exampleInput.clone());
		}
		List<ItemStack> returns = createLoredStacksForInfo(i);
		ItemStack toSt = tool.getItemStackRepresentation().get(0);
		for (String s : overwritenLore) {
			ItemAPI.addLore(toSt, s);
		}
		ItemAPI.addLore(toSt, ChatColor.GREEN + "Enough materials for " + new ItemMap(toSt).getMultiplesContainedIn(i)
				+ " runs");
		returns.add(toSt);
		return returns;
	}

	@Override
	public boolean applyEffect(Inventory i, FurnCraftChestFactory fccf) {
		logBeforeRecipeRun(i, fccf);
		if (input.removeSafelyFrom(i)) {
			ItemStack toolio = tool.getItemStackRepresentation().get(0);
			for (ItemStack is : i.getContents()) {
				if (is != null && toolio.getType() == is.getType() && hasStackRequiredLore(is)) {
					ItemMeta im = is.getItemMeta();
					if (im == null) {
						 im = Bukkit.getItemFactory().getItemMeta(is.getType());
					}
					List<String> currentLore = im.getLore();
					if (!overwritenLore.isEmpty()) {
						currentLore.removeAll(overwritenLore);
					}
					if (currentLore == null) {
						currentLore = new LinkedList<>();
					}
					currentLore.addAll(appliedLore);
					im.setLore(currentLore);
					is.setItemMeta(im);
					break;
				}
			}
		}
		logAfterRecipeRun(i, fccf);
		return true;
	}

	private boolean hasStackRequiredLore(ItemStack is) {
		if (is == null) {
			return false;
		}
		if (!is.hasItemMeta() && !overwritenLore.isEmpty()) {
			return false;
		}
		ItemMeta im = is.getItemMeta();
		if (!im.hasLore() && !overwritenLore.isEmpty()) {
			return false;
		}
		List<String> lore = im.getLore();
		if (im.hasLore()) {
			//check whether lore to apply preexists
			if (lore.containsAll(appliedLore)) {
				return false;
			}
		}
		if (overwritenLore.isEmpty()) {
			return true;
		}
		return lore.containsAll(overwritenLore);
	}

	@Override
	public String getTypeIdentifier() {
		return "LOREENCHANT";
	}

	public List <String> getAppliedLore() {
		return appliedLore;
	}

	public List <String> getOverwrittenLore() {
		return overwritenLore;
	}

	public ItemMap getTool() {
		return tool;
	}

	@Override
	public List<String> getTextualInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return Arrays.asList("1 " + ItemNames.getItemName(exampleInput));
	}

	@Override
	public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return Arrays.asList("1 " + ItemNames.getItemName(exampleOutput));
	}
}
