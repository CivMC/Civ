package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.igotyou.FactoryMod.Factory;
import com.github.igotyou.FactoryMod.utility.ItemMap;

/**
 * Used to decompact itemstacks, which means a single item with compacted lore
 * is turned into a whole stack without lore. This reverses the functionality of
 * the CompactingRecipe
 *
 */
public class DecompactingRecipe extends InputRecipe {
	private String compactedLore;

	public DecompactingRecipe(ItemMap input, String name, int productionTime,
			String compactedLore) {
		super(name, productionTime, input);
		this.compactedLore = compactedLore;
	}

	public boolean enoughMaterialAvailable(Inventory i) {
		if (!input.isContainedIn(i)) {
			return false;
		}
		for (ItemStack is : i.getContents()) {
			if (is != null) {
				ItemMeta im = is.getItemMeta();
				if (im.hasLore() && im.getLore().get(0).equals(compactedLore)) {
					return true;
				}
			}
		}
		return false;
	}

	public void applyEffect(Inventory i, Factory f) {
		if (input.isContainedIn(i)) {
			for (ItemStack is : i.getContents()) {
				if (is != null) {
					ItemMeta im = is.getItemMeta();
					if (im.hasLore()
							&& im.getLore().get(0).equals(compactedLore)) {
						List<String> loreList = new LinkedList<String>();
						ItemStack decompatedStack = is.clone();
						decompatedStack.setAmount(64);
						im.setLore(loreList);
						// not changing the original because getItemMeta() just
						// gives a copy
						decompatedStack.setItemMeta(im);
						if (new ItemMap(decompatedStack).fitsIn(i)) {
							for (ItemStack toRemove : input
									.getItemStackRepresentation()) {
								i.removeItem(toRemove);
							}
							ItemStack removeLoredStack = is.clone();
							removeLoredStack.setAmount(1);
							i.removeItem(removeLoredStack);
							i.addItem(decompatedStack);
						}
						break;
					}
				}
			}
		}

	}

	public List<ItemStack> getInputRepresentation(Inventory i) {
		List<ItemStack> result = new LinkedList<ItemStack>();
		result = createLoredStacksForInfo(i);
		for (ItemStack is : i.getContents()) {
			if (is != null) {
				ItemMeta im = is.getItemMeta();
				if (im.hasLore() && im.getLore().get(0).equals(compactedLore)) {
					ItemStack compactedStack = is.clone();
					result.add(compactedStack);
					break;
				}
			}
		}
		return result;
	}

	public ItemStack getRecipeRepresentation() {
		ItemStack res = new ItemStack(Material.CHEST);
		ItemMeta im = res.getItemMeta();
		im.setDisplayName(getRecipeName());
		res.setItemMeta(im);
		return res;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i) {
		List<ItemStack> result = new LinkedList<ItemStack>();
		for (ItemStack is : i.getContents()) {
			if (is != null) {
				ItemMeta im = is.getItemMeta();
				if (im.hasLore() && im.getLore().get(0).equals(compactedLore)) {
					ItemStack decompactedStack = is.clone();
					decompactedStack.setAmount(decompactedStack
							.getMaxStackSize());
					List<String> loreList = new LinkedList<String>();
					im.setLore(loreList);
					decompactedStack.setItemMeta(im);
					result.add(decompactedStack);
					break;
				}
			}
		}
		return result;
	}
}
