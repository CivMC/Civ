package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.igotyou.FactoryMod.factories.Factory;

public class DeterministicEnchantingRecipe extends InputRecipe {
	private Enchantment enchant;
	private int level;
	private ItemMap tool;

	public DeterministicEnchantingRecipe(String name, int productionTime,
			ItemMap input, ItemMap tool, Enchantment enchant, int level) {
		super(name, productionTime, input);
		this.enchant = enchant;
		this.tool = tool;
		this.level = level;
	}

	public boolean enoughMaterialAvailable(Inventory i) {
		if (input.isContainedIn(i)) {
			ItemStack toolio = tool.getItemStackRepresentation().get(0);
			for (ItemStack is : i.getContents()) {
				if (is != null
						&& toolio.getType() == is.getType()
						&& toolio.getEnchantmentLevel(enchant) == is
								.getEnchantmentLevel(enchant)) {
					return true;
				}
			}
		}
		return false;
	}

	public ItemStack getRecipeRepresentation() {
		ItemStack is = tool.getItemStackRepresentation().get(0);
		is.removeEnchantment(enchant);
		is.addEnchantment(enchant, level);
		ISUtils.setName(is, name);
		return is;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i) {
		ItemStack is = tool.getItemStackRepresentation().get(0);
		is.removeEnchantment(enchant);
		is.addEnchantment(enchant, level);
		if (i != null) {
			ISUtils.addLore(
					is,
					ChatColor.GREEN
							+ "Enough materials for "
							+ String.valueOf(Math.min(
									tool.getMultiplesContainedIn(i),
									input.getMultiplesContainedIn(i)))
							+ " runs");
		}
		List<ItemStack> stacks = new LinkedList<ItemStack>();
		stacks.add(is);
		return stacks;
	}

	public List<ItemStack> getInputRepresentation(Inventory i) {
		if (i == null) {
			List<ItemStack> bla = input.getItemStackRepresentation();
			bla.add(tool.getItemStackRepresentation().get(0));
			return bla;
		}
		List<ItemStack> returns = createLoredStacksForInfo(i);
		ItemStack toSt = tool.getItemStackRepresentation().get(0);
		ISUtils.addLore(toSt, ChatColor.GREEN + "Enough materials for "
				+ new ItemMap(toSt).getMultiplesContainedIn(i) + " runs");
		returns.add(toSt);
		return returns;
	}

	public void applyEffect(Inventory i, Factory f) {
		logBeforeRecipeRun(i, f);
		if (input.removeSafelyFrom(i)) {
			ItemStack toolio = tool.getItemStackRepresentation().get(0);
			for (ItemStack is : i.getContents()) {
				if (is != null
						&& toolio.getType() == is.getType()
						&& toolio.getEnchantmentLevel(enchant) == is
								.getEnchantmentLevel(enchant)) {
					ItemMeta im = is.getItemMeta();
					im.removeEnchant(enchant);
					im.addEnchant(enchant, level, true);
					is.setItemMeta(im);
					break;
				}
			}
		}
		logAfterRecipeRun(i, f);
	}

}
