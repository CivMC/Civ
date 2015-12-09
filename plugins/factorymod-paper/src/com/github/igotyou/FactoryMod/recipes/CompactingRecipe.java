package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.igotyou.FactoryMod.utility.ItemMap;

public class CompactingRecipe extends InputOutputRecipe {
	private List<Material> excludedMaterials;
	private String compactedLore;

	public CompactingRecipe(ItemMap input, List<Material> excludedMaterial,
			String name, int productionTime, String compactedLore) {
		super(name,productionTime,input);
		this.excludedMaterials = excludedMaterial;
		this.compactedLore = compactedLore;
	}

	public boolean enoughMaterialAvailable(Inventory i) {
		if (!input.isContainedIn(new ItemMap(i))) {
			return false;
		}
		for (ItemStack is : i.getContents()) {
			if (is != null) {
				if (compactable(is)) {
					return true;
				}
			}
		}
		return false;
	}

	public int getProductionTime() {
		return productionTime;
	}

	public String getRecipeName() {
		return name;
	}

	public void applyEffect(Inventory i) {
		if (input.isContainedIn(new ItemMap(i))) {
			for (ItemStack is : i.getContents()) {
				if (is != null) {
					if (compactable(is)) {
						List<String> loreList = new LinkedList<String>();
						loreList.add(compactedLore);
						for (ItemStack toRemove : input
								.getItemStackRepresentation()) {
							i.removeItem(toRemove);
						}
						compact(is);
					}
				}
			}
		}

	}

	public List<ItemStack> getInputRepresentation(Inventory i) {
		List<ItemStack> result = new LinkedList<ItemStack>();
		ItemMap inventoryMap = new ItemMap(i);
		if (input.isContainedIn(inventoryMap)) {
			result = createLoredStacksForInfo(i);
			for (ItemStack is : i.getContents()) {
				if (is != null) {
					if (compactable(is)) {
						ItemStack compactedStack = is.clone();
						result.add(compactedStack);
						break;
					}
				}
			}
		}
		return result;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i) {
		List<ItemStack> result = new LinkedList<ItemStack>();
		if (input.isContainedIn(new ItemMap(i))) {
			for (ItemStack is : i.getContents()) {
				if (is != null) {
					if (compactable(is)) {
						ItemStack decompactedStack = is.clone();
						compact(decompactedStack);
						result.add(decompactedStack);
						break;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Changes the lore of the given ItemStack to the compacted lore and sets
	 * it's amount to 1
	 * 
	 * @param is
	 */
	private void compact(ItemStack is) {
		List<String> loreList = new LinkedList<String>();
		loreList.add(compactedLore);
		is.setAmount(1);
		ItemMeta im = is.getItemMeta();
		im.setLore(loreList);
		is.setItemMeta(im);
	}

	/**
	 * Checks whether compacting a stack is allowed, which means it doesnt have
	 * meta data, it's a full stack and it's not on the list of excluded
	 * materials
	 * 
	 * @param is
	 *            ItemStack to check
	 * @return True if compacting the stack is allowed, false if not
	 */
	private boolean compactable(ItemStack is) {
		return !excludedMaterials.contains(is.getType())
				&& is.getAmount() == is.getMaxStackSize() && !is.hasItemMeta();
	}
}
