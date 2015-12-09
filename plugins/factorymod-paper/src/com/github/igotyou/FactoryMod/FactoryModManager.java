package com.github.igotyou.FactoryMod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.github.igotyou.FactoryMod.multiBlockStructures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.multiBlockStructures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.properties.AFactoryProperties;
import com.github.igotyou.FactoryMod.utility.ItemMap;

public abstract class FactoryModManager {

	protected FactoryModPlugin plugin;
	private HashMap<ItemMap, Factory> factoryCreationRecipes = new HashMap<ItemMap, Factory>();
	private HashMap<Material, HashMap<Location, Factory>> locations = new HashMap<Material, HashMap<Location, Factory>>();
	private HashSet<Material> possibleCenterBlocks;
	private HashSet<Material> possibleInteractionBlock;

	public FactoryModManager(FactoryModPlugin plugin) {
		this.plugin = plugin;
		// Normal furnace, craftingtable, chest factories
		possibleCenterBlocks.add(Material.WORKBENCH);
		possibleInteractionBlock.add(Material.WORKBENCH);
		possibleInteractionBlock.add(Material.FURNACE);
		possibleInteractionBlock.add(Material.BURNING_FURNACE);
		possibleInteractionBlock.add(Material.CHEST);
	}

	public void addFactory(Factory c, AFactoryProperties prop) {
		contraptions.put(c, prop);
	}

	public void removeFactory(Factory c) {
		contraptions.remove(c);
	}

	public Factory getFactoryAt(Location loc) {
		return getFactoryAt(loc.getBlock());
	}

	public Factory getFactoryAt(Block b) {
		HashMap<Location, Factory> forThisType = locations.get(b.getType());
		if (forThisType == null) {
			return null;
		} else {
			return forThisType.get(b.getLocation());
		}
	}

	public void addFactoryBlock(Block b, Factory c) {
		HashMap<Location, Factory> requiredMaterial = locations
				.get(b.getType());
		if (requiredMaterial == null) {
			requiredMaterial = new HashMap<Location, Factory>();
		}
		requiredMaterial.put(b.getLocation(), c);
	}

	public boolean factoryExistsAt(Location loc) {
		return getFactoryAt(loc) == null;
	}

	public void attemptCreation(Block b, Player p) {
		if (!factoryExistsAt(b.getLocation())) {
			// Cycle through possible structures here
			FurnCraftChestStructure fccs = new FurnCraftChestStructure(b);
			if (fccs.isComplete()) {

			}

		}
	}

	public boolean isPossibleCenterBlock(Material m) {
		return possibleCenterBlocks.contains(m);
	}

	public boolean isPossibleInteractionBlock(Material m) {
		return possibleInteractionBlock.contains(m);
	}
}
