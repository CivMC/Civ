package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class FactoryMaterialReturnRecipe extends InputRecipe {

	private double factor;

	public FactoryMaterialReturnRecipe(String identifier, String name, int productionTime,
			ItemMap input, double factor) {
		super(identifier, name, productionTime, input);
		this.factor = factor;
	}

	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		return createLoredStacksForInfo(i);
	}

	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			ItemStack is = new ItemStack(Material.PAPER);
			ISUtils.setName(is, "Total setupcost");
			ISUtils.addLore(is, ChatColor.AQUA + "All the materials invested into setting up and upgrading this factory");
			List <ItemStack> stacks = new LinkedList<ItemStack>();
			stacks.add(is);
			return stacks;
		}
		BlockState bs = (BlockState) i.getHolder();
		Location loc = bs.getLocation();
		FurnCraftChestFactory fcc = (FurnCraftChestFactory) FactoryMod
				.getManager().getFactoryAt(loc);
		return FactoryMod.getManager().getTotalSetupCost(fcc)
				.getItemStackRepresentation();
	}

	public ItemStack getRecipeRepresentation() {
		ItemStack is = new ItemStack(Material.WORKBENCH);
		ISUtils.setName(is, name);
		ItemMeta im = is.getItemMeta();
		im.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
		im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		is.setItemMeta(im);
		return is;
	}

	public void applyEffect(Inventory i, FurnCraftChestFactory fccf) {
		FactoryMod.getManager().removeFactory(fccf);
		for (Block b : fccf.getMultiBlockStructure().getRelevantBlocks()) {
			b.setType(Material.AIR);
		}
		Bukkit.getScheduler().runTaskLater(FactoryMod.getPlugin(),
				new Runnable() {
					@Override
					public void run() {
						Location dropLoc = fccf.getMultiBlockStructure()
								.getCenter();
						for (Entry<ItemStack, Integer> items : FactoryMod
								.getManager().getTotalSetupCost(fccf)
								.getEntrySet()) {
							int returnAmount = (int) (items.getValue() * factor);
							ItemMap im = new ItemMap();
							im.addItemAmount(items.getKey(), returnAmount);
							for (ItemStack is : im.getItemStackRepresentation()) {
								if (is.getDurability() == -1) {
									is.setDurability((short) 0);
								}
								dropLoc.getWorld().dropItemNaturally(dropLoc,
										is);
							}
						}
						dropLoc.getWorld().dropItemNaturally(dropLoc, new ItemStack(Material.WORKBENCH));
						dropLoc.getWorld().dropItemNaturally(dropLoc, new ItemStack(Material.FURNACE));
						dropLoc.getWorld().dropItemNaturally(dropLoc, new ItemStack(Material.CHEST));
					}
				}, 1L);
	}

	public double getFactor() {
		return factor;
	}

	@Override
	public String getTypeIdentifier() {
		return "COSTRETURN";
	}
}
