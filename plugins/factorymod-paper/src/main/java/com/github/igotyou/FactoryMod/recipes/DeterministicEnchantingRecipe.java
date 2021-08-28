package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class DeterministicEnchantingRecipe extends InputRecipe {
	private Enchantment enchant;
	private int level;
	private ItemMap tool;

	public DeterministicEnchantingRecipe(String identifier, String name, int productionTime, ItemMap input,
			ItemMap tool, Enchantment enchant, int level) {
		super(identifier, name, productionTime, input);
		this.enchant = enchant;
		this.tool = tool;
		this.level = level;
	}

	@Override
	public boolean enoughMaterialAvailable(Inventory inputInv) {
		if (input.isContainedIn(inputInv)) {
			ItemStack toolio = tool.getItemStackRepresentation().get(0);
			for (ItemStack is : inputInv.getContents()) {
				if (is != null && toolio.getType() == is.getType()
						&& toolio.getEnchantmentLevel(enchant) == is.getEnchantmentLevel(enchant)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public Material getRecipeRepresentationMaterial() {
		return tool.getItemStackRepresentation().get(0).getType();
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		ItemStack is = tool.getItemStackRepresentation().get(0);
		ItemMeta im = is.getItemMeta();
		im.removeEnchant(enchant);
		im.addEnchant(enchant, level, true);
		is.setItemMeta(im);
		if (i != null) {
			ItemUtils.addLore(is,
					ChatColor.GREEN + "Enough materials for "
							+ String.valueOf(
									Math.min(tool.getMultiplesContainedIn(i), input.getMultiplesContainedIn(i)))
							+ " runs");
		}
		List<ItemStack> stacks = new LinkedList<>();
		stacks.add(is);
		return stacks;
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			List<ItemStack> bla = input.getItemStackRepresentation();
			bla.add(tool.getItemStackRepresentation().get(0));
			return bla;
		}
		List<ItemStack> returns = createLoredStacksForInfo(i);
		ItemStack toSt = tool.getItemStackRepresentation().get(0);
		ItemUtils.addLore(toSt,
				ChatColor.GREEN + "Enough materials for " + new ItemMap(toSt).getMultiplesContainedIn(i) + " runs");
		returns.add(toSt);
		return returns;
	}

	@Override
	public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
		MultiInventoryWrapper combo = new MultiInventoryWrapper(inputInv, outputInv);
		logBeforeRecipeRun(combo, fccf);
		if (input.removeSafelyFrom(inputInv)) {
			ItemStack toolio = tool.getItemStackRepresentation().get(0);
			for (ItemStack is : inputInv.getContents()) {
				if (is != null && toolio.getType() == is.getType()
						&& toolio.getEnchantmentLevel(enchant) == is.getEnchantmentLevel(enchant)) {
					ItemMeta im = is.getItemMeta();
					im.removeEnchant(enchant);
					im.addEnchant(enchant, level, true);
					is.setItemMeta(im);
					break;
				}
			}
		}
		logAfterRecipeRun(combo, fccf);
		return true;
	}

	@Override
	public String getTypeIdentifier() {
		return "ENCHANT";
	}

	public int getLevel() {
		return level;
	}

	public Enchantment getEnchant() {
		return enchant;
	}

	public ItemMap getTool() {
		return tool;
	}

	@Override
	public List<String> getTextualInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List<String> res = super.getTextualInputRepresentation(i, fccf);
		res.add("1 " + ItemUtils.getItemName(tool.getItemStackRepresentation().get(0)));
		return res;
	}

	@Override
	public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return Arrays.asList("1 " + ItemUtils.getItemName(tool.getItemStackRepresentation().get(0)) + " with "
				+ enchant.toString() + " " + level);
	}
}
