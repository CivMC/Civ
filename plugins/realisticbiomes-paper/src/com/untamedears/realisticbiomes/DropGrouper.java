package com.untamedears.realisticbiomes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Group ItemStack drops.
 * Add items with add(), call done() when finished. The items will then get
 * merged asynchronously, and then get dropped as stacks synchronously.
 * Do not call out of order or more than once per instance.
 */
public class DropGrouper extends BukkitRunnable {

	private class Drop {
		private Location location;
		private Material material;
		private int amount;

		Drop(Location location, Material material) {
			this.location = location;
			this.material = material;
			amount = 1;
		}
	}
	
	private RealisticBiomes plugin;
	private World world;
	private List<Drop> drops = new ArrayList<Drop>();
	
	public DropGrouper(RealisticBiomes plugin, World world) {
		this.plugin = plugin;
		this.world = world;
	}
	
	public void add(Location location, Material material) {
		drops.add(new Drop(location, material));
	}
	
	public void done() {
		if (drops.size() > 0) {
			this.runTaskAsynchronously(plugin);
		}
	}

	@Override
	public void run() {
		HashMap<Material, List<Drop>> itemsMap = new HashMap<Material, List<Drop>>();
		for (Drop drop: drops) {
			List<Drop> list;
			if (!itemsMap.containsKey(drop.material)) {
				list = new ArrayList<Drop>();
				itemsMap.put(drop.material, list);
			} else {
				list = itemsMap.get(drop.material);
			}
			
			if (list.isEmpty()) {
				list.add(drop);
			} else {
				Drop lastItem = list.get(list.size() - 1);
				if (lastItem.amount < 64) {
					lastItem.amount += 1;
				} else {
					list.add(drop);
				}
			}
		}
		
		final List<Drop> items = new ArrayList<Drop>();
		for (Material key: itemsMap.keySet()) {
			items.addAll(itemsMap.get(key));
		}
		
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Drop drop: items) {
					RealisticBiomes.doLog(Level.FINEST, "DROPPING " + drop.amount + " " + drop.material + " at " + drop.location);
					world.dropItem(drop.location, new ItemStack(drop.material, drop.amount));
				}
			}
		}.runTask(plugin);
	}

}
