package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.igotyou.FactoryMod.factories.Factory;

public class LoreEnchantRecipe extends InputRecipe {

	private List<String> appliedLore;
	private List<String> overwritenLore;
	private ItemMap tool;

	public LoreEnchantRecipe(String identifier, String name, int productionTime, ItemMap input, ItemMap tool, List<String> appliedLore,
			List<String> overwritenLore) {
		super(identifier, name, productionTime, input);
		this.overwritenLore = overwritenLore;
		this.appliedLore = appliedLore;
		this.tool = tool;
	}

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

	public ItemStack getRecipeRepresentation() {
		ItemStack is = tool.getItemStackRepresentation().get(0);
		for (String s : appliedLore) {
			ISUtils.addLore(is, s);
		}
		ISUtils.setName(is, name);
		return is;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i) {
		ItemStack is = tool.getItemStackRepresentation().get(0);
		for (String s : appliedLore) {
			ISUtils.addLore(is, s);
		}
		if (i != null) {
			ISUtils.addLore(
					is,
					ChatColor.GREEN
							+ "Enough materials for "
							+ String.valueOf(Math.min(tool.getMultiplesContainedIn(i), input.getMultiplesContainedIn(i)))
							+ " runs");
		}
		List<ItemStack> stacks = new LinkedList<ItemStack>();
		stacks.add(is);
		return stacks;
	}

	public List<ItemStack> getInputRepresentation(Inventory i) {
		if (i == null) {
			List<ItemStack> bla = input.getItemStackRepresentation();
			ItemStack is = tool.getItemStackRepresentation().get(0);
			for (String s : overwritenLore) {
				ISUtils.addLore(is, s);
			}
			return bla;
		}
		List<ItemStack> returns = createLoredStacksForInfo(i);
		ItemStack toSt = tool.getItemStackRepresentation().get(0);
		for (String s : overwritenLore) {
			ISUtils.addLore(toSt, s);
		}
		ISUtils.addLore(toSt, ChatColor.GREEN + "Enough materials for " + new ItemMap(toSt).getMultiplesContainedIn(i)
				+ " runs");
		returns.add(toSt);
		return returns;
	}

	public void applyEffect(Inventory i, Factory f) {
		logBeforeRecipeRun(i, f);
		if (input.removeSafelyFrom(i)) {
			ItemStack toolio = tool.getItemStackRepresentation().get(0);
			for (ItemStack is : i.getContents()) {
				if (is != null && toolio.getType() == is.getType() && hasStackRequiredLore(toolio)) {
					ItemMeta im = is.getItemMeta();
					List<String> currentLore = im.getLore();
					if (overwritenLore.size() != 0) {
						currentLore.removeAll(overwritenLore);
					}
					currentLore.addAll(appliedLore);
					im.setLore(currentLore);
					is.setItemMeta(im);
					break;
				}
			}
		}
		logAfterRecipeRun(i, f);
	}

	private boolean hasStackRequiredLore(ItemStack is) {
		if (overwritenLore.size() == 0) {
			return true;
		}
		if (!is.hasItemMeta()) {
			return false;
		}
		ItemMeta im = is.getItemMeta();
		if (!im.hasLore()) {
			return false;
		}
		List<String> lore = im.getLore();
		return lore.containsAll(overwritenLore);
	}
}
