/**
 * @author Aleksey Terzi
 *
 */

package com.github.igotyou.FactoryMod.recipes;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public abstract class PrintingPressRecipe extends InputRecipe {
	public PrintingPressRecipe(String identifier, String name, int productionTime, ItemMap input) {
		super(identifier, name, productionTime, input);
	}

	protected ItemStack getPrintingPlateRepresentation(ItemMap printingPlate, String name) {
		List<ItemStack> out = printingPlate.getItemStackRepresentation();
		ItemStack res = out.size() == 0 ? new ItemStack(Material.STONE) : out.get(0).clone();
		ItemUtils.setDisplayName(res, name);

		return res;
	}
}
