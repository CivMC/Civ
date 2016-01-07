package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.utility.ItemMap;
import com.github.igotyou.FactoryMod.utility.ItemStackUtils;

public class Upgraderecipe extends InputRecipe {
	private IFactoryEgg egg;

	public Upgraderecipe(String name, int productionTime, ItemMap input, IFactoryEgg egg) {
		super(name, productionTime, input);
		this.egg = egg;
	}

	public void applyEffect(Inventory i, Factory f) {
		if (input.isContainedIn(i) && f instanceof FurnCraftChestFactory) {
			for (ItemStack is : input.getItemStackRepresentation()) {
				i.removeItem(is);
			}
			FurnCraftChestEgg e = (FurnCraftChestEgg) egg;
			((FurnCraftChestFactory) f).upgrade(e.getName(), e.getRecipes(),
					e.getFuel(), e.getFuelConsumptionIntervall(),
					e.getUpdateTime());
		}

	}

	public ItemStack getRecipeRepresentation() {
		ItemStack res = new ItemStack(Material.WORKBENCH);
		ItemStackUtils.setName(res, name);
		return res;
	}

	public List<ItemStack> getInputRepresentation(Inventory i) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		LinkedList<ItemStack> result = new LinkedList<ItemStack>();
		ItemMap inventoryMap = new ItemMap(i);
		ItemMap possibleRuns = new ItemMap();
		for (Entry<ItemStack, Integer> entry : input.getEntrySet()) {
			if (inventoryMap.getAmount(entry.getKey()) != 0) {
				possibleRuns.addItemAmount(
						entry.getKey(),
						inventoryMap.getAmount(entry.getKey())
								/ entry.getValue());
			} else {
				possibleRuns.addItemAmount(entry.getKey(), 0);
			}
		}
		for (ItemStack is : input.getItemStackRepresentation()) {
			if (possibleRuns.getAmount(is) != 0) {
				ItemStackUtils.addLore(is, ChatColor.GREEN
						+ "Enough of this material available to upgrade");
			} else {
				ItemStackUtils.addLore(is, ChatColor.RED
						+ "Not enough of this materials available to upgrade");
			}
			result.add(is);
		}
		return result;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i) {
		List<ItemStack> res = new LinkedList<ItemStack>();
		ItemStack cr = new ItemStack(Material.WORKBENCH);
		ItemStackUtils.setName(cr, egg.getName());
		ItemStackUtils.setLore(cr, ChatColor.LIGHT_PURPLE+ "Upgrade to get new and better recipes");
		res.add(cr);
		ItemStack fur = new ItemStack(Material.FURNACE);
		ItemStackUtils.setName(fur, egg.getName());
		ItemStackUtils.setLore(fur, ChatColor.LIGHT_PURPLE + "Recipes:");
		for(IRecipe rec : ((FurnCraftChestEgg)egg).getRecipes()) {
			ItemStackUtils.addLore(fur, ChatColor.YELLOW + rec.getRecipeName());
		}
		res.add(fur);
		ItemStack che = new ItemStack(Material.CHEST);
		ItemStackUtils.setLore(che, ChatColor.LIGHT_PURPLE + "Careful, you can not",ChatColor.LIGHT_PURPLE+ "revert upgrades!");
		ItemStackUtils.setName(che, egg.getName());
		res.add(che);
		return res;
	}

	public IFactoryEgg getEgg() {
		return egg;
	}

}
