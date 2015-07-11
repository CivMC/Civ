package com.untamedears.realisticbiomes;

import java.util.HashMap;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import com.google.common.collect.Lists;

/**
 * Group ItemStack drops.
 * Add items with add(), call done() when finished. The items will then get
 * dropped as stacks.
 */
public class DropGrouper {

	private class Drop {
		private Location location;
		private int amount;

		Drop(Location location) {
			this.location = location;
			amount = 1;
		}
	}
	
	private World world;
	private HashMap<Material, List<Drop>> drops = new HashMap<Material, List<Drop>>();
	
	public DropGrouper(World world) {
		this.world = world;
	}
	
	public void add(Location location, Material material) {
		if (drops.containsKey(material)) {
			List<Drop> list = drops.get(material);
			Drop drop = list.get(list.size() - 1);
			if (drop.amount < material.getMaxStackSize()) {
				drop.amount += 1;
			} else {
				list.add(new Drop(location));
			}
		} else {
			drops.put(material, Lists.newArrayList(new Drop(location)));
		}
	}
	
	public void done() {
		if (drops.size() > 0) {
			for (Material material: drops.keySet()) {
				List<Drop> list = drops.get(material);
				for (Drop drop: list) {
					world.dropItem(drop.location, new ItemStack(material, drop.amount));
				}
			}
		}
		drops = null; // calling more than once throws NPE by design
	}
}
