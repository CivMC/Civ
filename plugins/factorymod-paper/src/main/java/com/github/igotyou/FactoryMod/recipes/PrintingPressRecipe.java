/**
 * @author Aleksey Terzi
 *
 */

package com.github.igotyou.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.civmodcore.itemHandling.TagManager;

public abstract class PrintingPressRecipe extends InputRecipe {
	public PrintingPressRecipe(String identifier, String name, int productionTime, ItemMap input) {
		super(identifier, name, productionTime, input);
	}
	
	protected ItemStack getPrintingPlateRepresentation(ItemMap printingPlate, String name) {
		List<ItemStack> out = printingPlate.getItemStackRepresentation();
		ItemStack res = out.size() == 0 ? new ItemStack(Material.STONE) : out.get(0);
		TagManager tag = new TagManager();
		
		addEnchTag(tag);
		
		res = tag.enrichWithNBT(res);

		ISUtils.setName(res, name);
		
		return res;
	}
	
	protected static void addEnchTag(TagManager tag) {
		Map<String, Object> unb = new WeakHashMap<String, Object>();
		unb.put("id", (short)34);
		unb.put("lvl", (short)1);
		
		List<Object> ench = new ArrayList<Object>();
		ench.add(unb);
		
		tag.setList("ench", ench);
	}
}
