package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.utility.LoggingUtils;

public class DeterministicEnchantingRecipe extends InputRecipe {
	private Enchantment enchant;
	private int level;
	private Material tool;

	public DeterministicEnchantingRecipe(String name, int productionTime,
			ItemMap input, Material tool, Enchantment enchant, int level) {
		super(name, productionTime, input);
		this.enchant = enchant;
		this.tool = tool;
		this.level = level;
	}

	public boolean enoughMaterialAvailable(Inventory i) {
		if (input.isContainedIn(i)) {
			for (ItemStack is : i.getContents()) {
				if (is != null && is.getType() == tool
						&& is.getItemMeta().getEnchantLevel(enchant) < level) {
					return true;
				}
			}
		}
		return false;
	}

	public ItemStack getRecipeRepresentation() {
		ItemStack is = new ItemStack(tool);
		is.addEnchantment(enchant, level);
		ISUtils.setName(is, name);
		return is;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i) {
		ItemStack is = new ItemStack(tool);
		is.addEnchantment(enchant, level);
		if (i != null) {
			ISUtils.addLore(
					is,
					ChatColor.GREEN
							+ "Enough materials for "
							+ String.valueOf(Integer.max(new ItemMap(
									new ItemStack(tool))
									.getMultiplesContainedIn(i), input
									.getMultiplesContainedIn(i))) + " runs");
		}
		List <ItemStack> stacks = new LinkedList<ItemStack>();
		stacks.add(is);
		return stacks;
	}

	public List<ItemStack> getInputRepresentation(Inventory i) {
		if (i == null) {
			List<ItemStack> bla = input.getItemStackRepresentation();
			bla.add(new ItemStack(tool));
			return bla;
		}
		List<ItemStack> returns = createLoredStacksForInfo(i);
		ItemStack toSt = new ItemStack(tool);
		ISUtils.addLore(toSt, ChatColor.GREEN + "Enough materials for "
				+ new ItemMap(toSt).getMultiplesContainedIn(i) + " runs");
		returns.add(toSt);
		return returns;
	}

	public void applyEffect(Inventory i, Factory f) {
		logBeforeRecipeRun(i, f);
		for(ItemStack is:input.getItemStackRepresentation()) {
			i.removeItem(is);
		}
		for(ItemStack is:i.getContents()) {
			if (is != null && is.getType() == tool && is.getItemMeta().getEnchantLevel(enchant) < level) {
				is.getItemMeta().removeEnchant(enchant);
				is.getItemMeta().addEnchant(enchant, level, true);
				break;
			}
		}
		logAfterRecipeRun(i, f);
	}

}
