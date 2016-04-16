package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.igotyou.FactoryMod.factories.Factory;

/**
 * Used to "compact" itemstack, which means complete stacks are reduced to a
 * single lored item, which can be stacked. This makes the transportation of
 * those items much easier. Additionally there can be a cost for each
 * compaction.
 *
 */
public class CompactingRecipe extends InputRecipe {
	private List<Material> excludedMaterials;
	private String compactedLore;

	public CompactingRecipe(ItemMap input, List<Material> excludedMaterial,
			String name, int productionTime, String compactedLore) {
		super(name, productionTime, input);
		this.excludedMaterials = excludedMaterial;
		this.compactedLore = compactedLore;
	}

	public boolean enoughMaterialAvailable(Inventory i) {
		if (!input.isContainedIn(i)) {
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

	public void applyEffect(Inventory i, Factory f) {
		logBeforeRecipeRun(i, f);
		if (input.isContainedIn(i)) {
			for (ItemStack is : i.getContents()) {
				if (is != null) {
					if (compactable(is)) {
						if (input.removeSafelyFrom(i)) {
							compact(is);
						}
						break;
					}
				}
			}
		}
		logAfterRecipeRun(i, f);

	}

	public List<ItemStack> getInputRepresentation(Inventory i) {
		List<ItemStack> result = new LinkedList<ItemStack>();
		if (i == null) {
			result.add(new ItemStack(Material.STONE, 64));
			result.addAll(input.getItemStackRepresentation());
			return result;
		}
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
		return result;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i) {
		List<ItemStack> result = new LinkedList<ItemStack>();
		if (i == null) {
			ItemStack is = new ItemStack(Material.STONE, 64);
			compact(is);
			result.add(is);
			return result;
		}
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

		return result;
	}

	public ItemStack getRecipeRepresentation() {
		ItemStack res = new ItemStack(Material.CHEST);
		ItemMeta im = res.getItemMeta();
		im.setDisplayName(getRecipeName());
		res.setItemMeta(im);
		return res;
	}

	/**
	 * Changes the lore of the given ItemStack to the compacted lore and sets
	 * it's amount to 1
	 * 
	 * @param is
	 */
	private void compact(ItemStack is) {
		ISUtils.addLore(is, compactedLore);
		is.setAmount(1);
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
		return is.getMaxStackSize() != 1
				&& !excludedMaterials.contains(is.getType())
				&& is.getAmount() == is.getMaxStackSize() && !is.hasItemMeta();
	}
}
