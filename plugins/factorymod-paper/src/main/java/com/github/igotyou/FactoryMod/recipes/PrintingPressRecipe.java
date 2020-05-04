/**
 * @author Aleksey Terzi
 *
 */

package com.github.igotyou.FactoryMod.recipes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.civmodcore.itemHandling.TagManager;

import java.util.List;

public abstract class PrintingPressRecipe extends InputRecipe {
	public PrintingPressRecipe(String identifier, String name, int productionTime, ItemMap input) {
		super(identifier, name, productionTime, input);
	}

	protected ItemStack getPrintingPlateRepresentation(ItemMap printingPlate, String name) {
		List<ItemStack> out = printingPlate.getItemStackRepresentation();
		ItemStack res = out.size() == 0 ? new ItemStack(Material.STONE) : out.get(0);
		TagManager tag = new TagManager();

		res = tag.enrichWithNBT(res);

		ItemAPI.setDisplayName(res, name);

		return res;
	}
}
