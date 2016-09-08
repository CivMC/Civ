package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

public class Upgraderecipe extends InputRecipe {
	private IFactoryEgg egg;

	public Upgraderecipe(String identifier, String name, int productionTime, ItemMap input,
			IFactoryEgg egg) {
		super(identifier, name, productionTime, input);
		this.egg = egg;
	}

	public void applyEffect(Inventory i, Factory f) {
		logAfterRecipeRun(i, f);
		if (input.isContainedIn(i) && f instanceof FurnCraftChestFactory) {
			if (input.removeSafelyFrom(i)) {
				FurnCraftChestEgg e = (FurnCraftChestEgg) egg;
				((FurnCraftChestFactory) f).upgrade(e.getName(),
						e.getRecipes(), e.getFuel(),
						e.getFuelConsumptionIntervall(), e.getUpdateTime(), e.getMaximumHealth(), 
						e.getDamagePerDamagingPeriod(), e.getBreakGracePeriod(), e.getCitadelBreakReduction());
			}
		}
		logAfterRecipeRun(i, f);
	}

	public ItemStack getRecipeRepresentation() {
		ItemStack res = ((InputRecipe)((FurnCraftChestEgg)egg).getRecipes().get(0)).getOutputRepresentation(null).get(0);
		res.setAmount(1);
		ItemMeta im = res.getItemMeta();
		im.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
		im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		res.setItemMeta(im);
		ISUtils.setName(res, name);
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
				ISUtils.addLore(is, ChatColor.GREEN
						+ "Enough of this material available to upgrade");
			} else {
				ISUtils.addLore(is, ChatColor.RED
						+ "Not enough of this materials available to upgrade");
			}
			result.add(is);
		}
		return result;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i) {
		List<ItemStack> res = new LinkedList<ItemStack>();
		ItemStack cr = new ItemStack(Material.WORKBENCH);
		ISUtils.setName(cr, egg.getName());
		ISUtils.setLore(cr, ChatColor.LIGHT_PURPLE
				+ "Upgrade to get new and better recipes");
		res.add(cr);
		ItemStack fur = new ItemStack(Material.FURNACE);
		ISUtils.setName(fur, egg.getName());
		ISUtils.setLore(fur, ChatColor.LIGHT_PURPLE + "Recipes:");
		for (IRecipe rec : ((FurnCraftChestEgg) egg).getRecipes()) {
			ISUtils.addLore(fur, ChatColor.YELLOW + rec.getName());
		}
		res.add(fur);
		ItemStack che = new ItemStack(Material.CHEST);
		ISUtils.setLore(che, ChatColor.LIGHT_PURPLE + "Careful, you can not",
				ChatColor.LIGHT_PURPLE + "revert upgrades!");
		ISUtils.setName(che, egg.getName());
		res.add(che);
		return res;
	}

	public IFactoryEgg getEgg() {
		return egg;
	}
	
	@Override
	public String getTypeIdentifier() {
		return "UPGRADE";
	}

}
