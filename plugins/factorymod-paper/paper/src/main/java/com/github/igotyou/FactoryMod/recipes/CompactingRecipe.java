package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

import java.util.*;

import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;

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

	@Override
	public boolean enoughMaterialAvailable(Inventory inputInv) {
		if (!input.isContainedIn(inputInv)) {
			return false;
		}
		ItemMap im = new ItemMap(inputInv);
		for (ItemStack is : inputInv.getContents()) {
			if (is != null) {
				if (compactable(is, im)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int getProductionTime() {
		return productionTime;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
		MultiInventoryWrapper combo = new MultiInventoryWrapper(inputInv, outputInv);
		logBeforeRecipeRun(combo, fccf);
		if (input.isContainedIn(inputInv)) {
			ItemMap im = new ItemMap(inputInv);
			//technically we could just directly work with the ItemMap here to iterate over the items so we dont check identical items multiple times,
			//but using the iterator of the inventory preserves the order of the inventory, which is more important here to guarantee one behavior
			//to the player
			for (ItemStack is : inputInv.getContents()) {
				if (is != null) {
					if (compactable(is, im)) {
						if (input.removeSafelyFrom(inputInv)) {
							compact(is, inputInv, outputInv);
						}
						break;
					}
				}
			}
		}
		logAfterRecipeRun(combo, fccf);
		return true;
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List<ItemStack> result = new LinkedList<>();
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

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List<ItemStack> result = new LinkedList<>();
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

	@Override
	public Material getRecipeRepresentationMaterial() {
		return Material.CHEST;
	}

	/**
	 * Removes the amount required to compact the given ItemStack from the given inventory and adds a comapcted item to the inventory
	 * 
	 * @param is
	 */
	private void compact(ItemStack is, Inventory inputInv, Inventory outputInv) {
		ItemStack copy = is.clone();
		copy.setAmount(getCompactStackSize(copy.getType()));
		ItemMap toRemove = new ItemMap(copy);
		if (toRemove.removeSafelyFrom(inputInv)) {
			compactStack(copy);
			outputInv.addItem(copy);
		}
	}

	/**
	 * Applies the lore and set the amount to 1. Dont call this directly if you want to compact items for players
	 */
	private void compactStack(ItemStack is) {
		ItemUtils.addLore(is,compactedLore);
		is.setAmount(1);
	}

	public static int getCompactStackSize(Material m) {
		switch (m.getMaxStackSize()) {
			case 64: return 64;
			case 16: return 16;
			case 1: return 8;
			default:
				FactoryMod.getInstance().warning("Unknown max stacksize for type " + m.toString());
		}
		return 999_999; //prevents compacting in error case, because never enough will fit in a chest
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
				is.getItemMeta().getLore().contains(compactedLore))) {
			return false;
		}
        return im.getAmount(is) >= getCompactStackSize(is.getType());
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
	
	@Override
	public List<String> getTextualInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return Arrays.asList("An entire stack of a stackable item", "---OR---", "Eight of a non stackable item");
	}

	@Override
	public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return Arrays.asList("Input stack compacted into a single item");
	}
}
