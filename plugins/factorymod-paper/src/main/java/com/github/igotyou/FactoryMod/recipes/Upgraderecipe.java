package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class Upgraderecipe extends InputRecipe {
	private FurnCraftChestEgg egg;

	public Upgraderecipe(String identifier, String name, int productionTime, ItemMap input,
			FurnCraftChestEgg egg) {
		super(identifier, name, productionTime, input);
		this.egg = egg;
	}

	@Override
	public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
		MultiInventoryWrapper combo = new MultiInventoryWrapper(inputInv, outputInv);
		logBeforeRecipeRun(combo, fccf);
		if (input.isContainedIn(inputInv)) {
			if (input.removeSafelyFrom(inputInv)) {
				FurnCraftChestEgg e = egg;
				fccf.upgrade(e.getName(),
						e.getRecipes(), e.getFuel(),
						e.getFuelConsumptionIntervall(), e.getUpdateTime(), e.getMaximumHealth(), 
						e.getDamagePerDamagingPeriod(), e.getBreakGracePeriod(), e.getCitadelBreakReduction());
			}
		}
		logAfterRecipeRun(combo, fccf);
		return true;
	}

	@Override
	public ItemStack getRecipeRepresentation() {
		ItemStack res = ((InputRecipe)egg.getRecipes().get(0)).getOutputRepresentation(null, null).get(0);
		res.setAmount(1);
		ItemMeta im = res.getItemMeta();
		im.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
		im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		res.setItemMeta(im);
		ItemUtils.setDisplayName(res, name);
		return res;
	}
	
	@Override
	public Material getRecipeRepresentationMaterial() {
		return ((InputRecipe)egg.getRecipes().get(0)).getOutputRepresentation(null, null).get(0).getType();
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		LinkedList<ItemStack> result = new LinkedList<>();
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
				ItemUtils.addLore(is, ChatColor.GREEN
						+ "Enough of this material available to upgrade");
			} else {
				ItemUtils.addLore(is, ChatColor.RED
						+ "Not enough of this materials available to upgrade");
			}
			result.add(is);
		}
		return result;
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List<ItemStack> res = new LinkedList<>();
		ItemStack cr = new ItemStack(Material.CRAFTING_TABLE);
		ItemUtils.setDisplayName(cr, egg.getName());
		ItemUtils.setLore(cr, ChatColor.LIGHT_PURPLE
				+ "Upgrade to get new and better recipes");
		res.add(cr);
		ItemStack fur = new ItemStack(Material.FURNACE);
		ItemUtils.setDisplayName(fur, egg.getName());
		ItemUtils.setLore(fur, ChatColor.LIGHT_PURPLE + "Recipes:");
		for (IRecipe rec : egg.getRecipes()) {
			ItemUtils.addLore(fur, ChatColor.YELLOW + rec.getName());
		}
		res.add(fur);
		ItemStack che = new ItemStack(Material.CHEST);
		ItemUtils.setLore(che, ChatColor.LIGHT_PURPLE + "Careful, you can not",
				ChatColor.LIGHT_PURPLE + "revert upgrades!");
		ItemUtils.setDisplayName(che, egg.getName());
		res.add(che);
		return res;
	}

	public FurnCraftChestEgg getEgg() {
		return egg;
	}

	@Override
	public String getTypeIdentifier() {
		return "UPGRADE";
	}

	@Override
	public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return Arrays.asList("Upgrades the factory to " + egg.getName());
	}

}
