package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class FactoryMaterialReturnRecipe extends InputRecipe {

	private double factor;

	public FactoryMaterialReturnRecipe(String identifier, String name, int productionTime,
			ItemMap input, double factor) {
		super(identifier, name, productionTime, input);
		this.factor = factor;
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		return createLoredStacksForInfo(i);
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			ItemStack is = new ItemStack(Material.PAPER);
			ItemUtils.setDisplayName(is, "Total setupcost");
			ItemUtils.addLore(is, ChatColor.AQUA + "All the materials invested into setting up and upgrading this factory");
			List <ItemStack> stacks = new LinkedList<>();
			stacks.add(is);
			return stacks;
		}
		InventoryHolder ih = i.getHolder();
		Location loc = null;
		if (ih instanceof BlockState) {
			BlockState bs = (BlockState) ih;
			loc = bs.getLocation();
		} else if (ih instanceof DoubleChest) {
			DoubleChest dc = (DoubleChest) ih;
			loc = dc.getLocation();
		}
		FurnCraftChestFactory fcc = (loc != null ? (FurnCraftChestFactory) FactoryMod
				.getInstance().getManager().getFactoryAt(loc) : fccf);
		return FactoryMod.getInstance().getManager().getTotalSetupCost(fcc)
				.getItemStackRepresentation();
	}

	@Override
	public Material getRecipeRepresentationMaterial() {
		return Material.CRAFTING_TABLE;
	}

	@Override
	public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
		FactoryMod.getInstance().getManager().removeFactory(fccf);
		for (Block b : fccf.getMultiBlockStructure().getRelevantBlocks()) {
			b.setType(Material.AIR);
		}
		Bukkit.getScheduler().runTaskLater(FactoryMod.getInstance(),
				new Runnable() {
					@Override
					public void run() {
						Location dropLoc = fccf.getMultiBlockStructure()
								.getCenter();
						for (Entry<ItemStack, Integer> items : FactoryMod.getInstance().
								getManager().getTotalSetupCost(fccf)
								.getEntrySet()) {
							int returnAmount = (int) (items.getValue() * factor);
							ItemMap im = new ItemMap();
							im.addItemAmount(items.getKey(), returnAmount);
							for (ItemStack is : im.getItemStackRepresentation()) {
								dropLoc.getWorld().dropItemNaturally(dropLoc,
										is);
							}
						}
						dropLoc.getWorld().dropItemNaturally(dropLoc, new ItemStack(Material.CRAFTING_TABLE));
						dropLoc.getWorld().dropItemNaturally(dropLoc, new ItemStack(Material.FURNACE));
						dropLoc.getWorld().dropItemNaturally(dropLoc, new ItemStack((fccf.getChest()).getType()));
					}
				}, 1L);
		return true;
	}

	public double getFactor() {
		return factor;
	}

	@Override
	public String getTypeIdentifier() {
		return "COSTRETURN";
	}

	@Override
	public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return Arrays.asList(factor * 100 + " % of the factories setup cost");
	}
}
