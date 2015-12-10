package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.igotyou.FactoryMod.Factory;
import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.utility.ItemMap;

public class RepairRecipe extends InputRecipe {
	private int healthPerRun;

	public RepairRecipe(String name, int productionTime, ItemMap input,
			int healthPerRun) {
		super(name, productionTime, input);
		this.healthPerRun = healthPerRun;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i) {
		List<ItemStack> result = new LinkedList<ItemStack>();
		ItemStack furn = new ItemStack(Material.FURNACE);
		ItemMeta im = furn.getItemMeta();
		List <String> lore = new LinkedList<String>();
		lore.add("+"+String.valueOf(healthPerRun)+" health");
		im.setLore(lore);
		furn.setItemMeta(im);
		result.add(furn);
		return result;
	}

	public List<ItemStack> getInputRepresentation(Inventory i) {
		return createLoredStacksForInfo(i);
	}

	public void applyEffect(Inventory i) {
		if (enoughMaterialAvailable(i)) {
			for (ItemStack is : input.getItemStackRepresentation()) {
				i.removeItem(is);
			}
			BlockState bs = (BlockState) i.getHolder();
			Factory f = FactoryModPlugin.getManager().getFactoryAt(
					bs.getLocation());
			f.getRepairManager().repair(healthPerRun);
		}
	}
	
	public ItemStack getRecipeRepresentation() {
		ItemStack res = new ItemStack(Material.FURNACE);
		ItemMeta im = res.getItemMeta();
		im.setDisplayName(getRecipeName());
		res.setItemMeta(im);
		return res;
	}
}
