package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.Factory;

/**
 * Used to compact items, which means whole or multiple stacks of an item are reduced to a single lored item, which is stackable to the same stacksize
 * As the original material. This makes the transportation of
 * those items much easier, additionally there can be a cost for each
 * compaction. Items that stack to 64 and 16 will be compacted per stack and items that stack to 1 will be compacted with a 8:1 ratio 
 *
 */
public class CompactingRecipe extends InputRecipe {
	private List<Material> excludedMaterials;
	private String compactedLore;

	public CompactingRecipe(String identifier, ItemMap input, List<Material> excludedMaterial,
			String name, int productionTime, String compactedLore) {
		super(identifier, name, productionTime, input);
		this.excludedMaterials = excludedMaterial;
		this.compactedLore = compactedLore;
	}

	public boolean enoughMaterialAvailable(Inventory i) {
		if (!input.isContainedIn(i)) {
			return false;
		}
		ItemMap im = new ItemMap(i);
		for (ItemStack is : i.getContents()) {
			if (is != null) {
				if (compactable(is, im)) {
					return true;
				}
			}
		}
		return false;
	}

	public int getProductionTime() {
		return productionTime;
	}

	public String getName() {
		return name;
	}

	public void applyEffect(Inventory i, Factory f) {
		logBeforeRecipeRun(i, f);
		if (input.isContainedIn(i)) {
			ItemMap im = new ItemMap(i);
			//technically we could just directly work with the ItemMap here to iterate over the items so we dont check identical items multiple times,
			//but using the iterator of the inventory preserves the order of the inventory, which is more important here to guarantee one behavior
			//to the player
			for (ItemStack is : i.getContents()) {
				if (is != null) {
					if (compactable(is, im)) {
						if (input.removeSafelyFrom(i)) {
							compact(is,i);
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
		ItemMap im = new ItemMap(i);
		for (ItemStack is : i.getContents()) {
			if (is != null) {
				if (compactable(is, im)) {
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
			compactStack(is);
			result.add(is);
			return result;
		}
		ItemMap im = new ItemMap(i);
		for (ItemStack is : i.getContents()) {
			if (is != null) {
				if (compactable(is, im)) {
					ItemStack decompactedStack = is.clone();
					compactStack(decompactedStack);
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
		im.setDisplayName(getName());
		res.setItemMeta(im);
		return res;
	}

	/**
	 * Removes the amount required to compact the given ItemStack from the given inventory and adds a comapcted item to the inventory
	 * 
	 * @param is
	 */
	private void compact(ItemStack is, Inventory i) {
		ItemStack copy = is.clone();
		copy.setAmount(getCompactStackSize(copy.getType()));
		ItemMap toRemove = new ItemMap(copy);
		if (toRemove.removeSafelyFrom(i)) {
			compactStack(copy);
			i.addItem(copy);
		}
	}
	
	/**
	 * Applies the lore and set the amount to 1. Dont call this directly if you want to compact items for players
	 */
	private void compactStack(ItemStack is) {
		ISUtils.addLore(is,compactedLore);
		is.setAmount(1);
	}

	public static int getCompactStackSize(Material m) {
		switch (m.getMaxStackSize()) {
			case 64: return 64;
			case 16: return 16;
			case 1: return 8;
			default:
				FactoryMod.getPlugin().warning("Unknown max stacksize for type " + m.toString());
		}
		return 999999; //prevents compacting in error case, because never enough will fit in a chest
	}
	/**
	 * Checks whether enough of a certain item stack is available to compact it
	 * 
	 * @param is
	 *            ItemStack to check
	 * @param im 
	 * 	      ItemMap representing the inventory from which is compacted
	 * @return True if compacting the stack is allowed, false if not
	 */
	private boolean compactable(ItemStack is, ItemMap im) {
		if (is == null || excludedMaterials.contains(is.getType()) || (input.getAmount(is) != 0) || (is.getItemMeta().getLore() != null &&
				is.getItemMeta().getLore().contains(compactedLore)) || (is.getItemMeta().hasEnchants() && is.getType().getMaxStackSize() == 1)) {
			return false;
		}	
		if (im.getAmount(is) >= getCompactStackSize(is.getType())) {
			return true;
		} 
		return false;
	}

	@Override
	public String getTypeIdentifier() {
		return "COMPACT";
	}
	
	public String getCompactedLore() {
		return compactedLore;
	}
	
	public List <Material> getExcludedMaterials() {
		return excludedMaterials;
	}
}