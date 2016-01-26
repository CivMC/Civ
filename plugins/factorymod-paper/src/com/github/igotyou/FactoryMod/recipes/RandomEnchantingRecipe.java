package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.civmodcore.itemHandling.NiceNames;

import com.github.igotyou.FactoryMod.factories.Factory;

public class RandomEnchantingRecipe extends InputRecipe {
	private List<RandomEnchant> enchants;
	private Material tool;
	private static Random rng;

	public class RandomEnchant {
		private Enchantment enchant;
		private int level;
		private double chance;

		public RandomEnchant(Enchantment enchant, int level, double chance) {
			this.enchant = enchant;
			this.level = level;
			this.chance = chance;
		}
	}

	public RandomEnchantingRecipe(String name, int productionTime,
			ItemMap input, Material tool, List<RandomEnchant> enchants) {
		super(name, productionTime, input);
		this.enchants = enchants;
		this.tool = tool;
		if (rng == null) {
			rng = new Random();
		}
	}

	public ItemStack getRecipeRepresentation() {
		ItemStack is = new ItemStack(tool);
		for (RandomEnchant re : enchants) {
			is.addEnchantment(re.enchant, re.level);
		}
		ISUtils.setName(is, name);
		return is;
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

	public List<ItemStack> getOutputRepresentation(Inventory i) {
		ItemStack is = new ItemStack(tool);
		for (RandomEnchant re : enchants) {
			is.addEnchantment(re.enchant, re.level);
		}
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
		for (RandomEnchant re : enchants) {
			ISUtils.addLore(is,
					ChatColor.YELLOW + String.valueOf(re.chance * 100)
							+ " % chance for " + NiceNames.getName(re.enchant)
							+ " " + String.valueOf(re.level));
		}
		ISUtils.addLore(is, ChatColor.LIGHT_PURPLE
				+ "At least one guaranteed");
		List<ItemStack> stacks = new LinkedList<ItemStack>();
		stacks.add(is);
		return stacks;
	}

	public void applyEffect(Inventory i, Factory f) {
		logBeforeRecipeRun(i, f);
		for (ItemStack is : input.getItemStackRepresentation()) {
			i.removeItem(is);
		}
		for (ItemStack is : i.getContents()) {
			if (is != null && is.getType() == tool
					&& !is.getItemMeta().hasEnchants()) {
				boolean applied = false;
				while (!applied) {
					for (RandomEnchant re : enchants) {
						if (rng.nextDouble() <= re.chance) {
							is.getItemMeta().addEnchant(re.enchant, re.level,
									true);
							applied = true;
						}
					}
				}
				break;
			}
		}
		logAfterRecipeRun(i, f);
	}

}
