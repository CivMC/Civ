package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.igotyou.FactoryMod.utility.ItemMap;

public class ProductionRecipe extends InputRecipe {
	private ItemMap output;

	public ProductionRecipe(String name, int productionTime, ItemMap inputs,
			ItemMap output) {
		super(name, productionTime, inputs);
		this.output = output;
	}

	public ItemMap getOutput() {
		return output;
	}

	public int getCurrentMultiplier(Inventory i) {
		ItemMap im = new ItemMap(i);
		return input.getMultiplesContainedIn(im);
	}

	public ItemMap getCurrentOutput(Inventory i) {
		ItemMap copy = output.clone();
		copy.multiplyContent(getCurrentMultiplier(i));
		return copy;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i) {
		List <ItemStack> stacks = output.getItemStackRepresentation();
		int possibleRuns = input.getMultiplesContainedIn(new ItemMap (i));
		for(ItemStack is: stacks) {
			ItemMeta im = is.getItemMeta();
			List<String> lore;
			if (im.hasLore()) {
				lore = im.getLore();
			} else {
				lore = new LinkedList<String>();
			}
			lore.add(ChatColor.GREEN + "Enough materials for "
					+ String.valueOf(possibleRuns) + " runs");
			im.setLore(lore);
			is.setItemMeta(im);
		}
		return stacks;
	}

	public List<ItemStack> getInputRepresentation(Inventory i) {
		return createLoredStacksForInfo(i);
	}

	public void applyEffect(Inventory i) {
		ItemMap toRemove = input.clone();
		ItemMap toAdd = output.clone();
		if (new ItemMap(i).contains(toRemove))
			for (ItemStack is : toRemove.getItemStackRepresentation()) {
				i.removeItem(is);
			}
		for (ItemStack is : toAdd.getItemStackRepresentation()) {
			i.addItem(is);
		}
	}
	
	public ItemStack getRecipeRepresentation() {
		List <ItemStack> out = output.getItemStackRepresentation();
		ItemStack res;
		if (out.size() == 0) {
			res = new ItemStack(Material.STONE);
		}
		else {
			res = out.get(0);
		}
		ItemMeta im = res.getItemMeta();
		im.setDisplayName(getRecipeName());
		res.setItemMeta(im);
		return res;
	}
}
