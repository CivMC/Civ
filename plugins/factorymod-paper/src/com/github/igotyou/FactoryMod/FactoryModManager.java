package com.github.igotyou.FactoryMod;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.multiBlockStructures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.multiBlockStructures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.utility.ItemMap;

public class FactoryModManager {

	protected FactoryModPlugin plugin;
	private HashMap<Class<MultiBlockStructure>, HashMap<ItemMap, IFactoryEgg>> factoryCreationRecipes;
	private HashMap<Material, HashMap<Location, Factory>> locations;
	private HashMap<String, IFactoryEgg> eggs;
	private HashSet<Material> possibleCenterBlocks;
	private HashSet<Material> possibleInteractionBlock;
	private Material factoryInteractionMaterial;
	private boolean citadelEnabled;
	private String compactLore;

	public FactoryModManager(FactoryModPlugin plugin,
			Material factoryInteractionMaterial, boolean citadelEnabled) {
		this.plugin = plugin;
		this.factoryInteractionMaterial = factoryInteractionMaterial;
		this.citadelEnabled = citadelEnabled;

		factoryCreationRecipes = new HashMap<Class<MultiBlockStructure>, HashMap<ItemMap, IFactoryEgg>>();
		locations = new HashMap<Material, HashMap<Location, Factory>>();
		eggs = new HashMap<String, IFactoryEgg>();
		possibleCenterBlocks = new HashSet<Material>();
		possibleInteractionBlock = new HashSet<Material>();

		// Normal furnace, craftingtable, chest factories
		possibleCenterBlocks.add(Material.WORKBENCH);
		possibleInteractionBlock.add(Material.WORKBENCH);
		possibleInteractionBlock.add(Material.FURNACE);
		possibleInteractionBlock.add(Material.BURNING_FURNACE);
		possibleInteractionBlock.add(Material.CHEST);
	}

	public void setCompactLore(String lore) {
		compactLore = lore;
	}

	public String getCompactLore() {
		return compactLore;
	}

	public void addFactory(Factory f) {
		for (Block b : f.getMultiBlockStructure().getAllBlocks()) {
			HashMap<Location, Factory> forThatMaterial = locations.get(b
					.getType());
			if (forThatMaterial == null) {
				forThatMaterial = new HashMap<Location, Factory>();
				locations.put(b.getType(), forThatMaterial);
			}
			forThatMaterial.put(b.getLocation(), f);
		}
	}

	public boolean isCitadelEnabled() {
		return citadelEnabled;
	}

	public Material getFactoryInteractionMaterial() {
		return factoryInteractionMaterial;
	}

	public void removeFactory(Factory f) {
		for (Block b : f.getMultiBlockStructure().getAllBlocks()) {
			HashMap<Location, Factory> forThatMaterial = locations.get(b
					.getType());
			forThatMaterial.remove(b.getLocation());
		}
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

	public boolean factoryExistsAt(Location loc) {
		return getFactoryAt(loc) == null;
	}

	public void attemptCreation(Block b, Player p) {
		if (!factoryExistsAt(b.getLocation())) {
			// Cycle through possible structures here
			FurnCraftChestStructure fccs = new FurnCraftChestStructure(b);
			if (fccs.isComplete()) {
				HashMap<ItemMap, IFactoryEgg> eggs = factoryCreationRecipes
						.get(FurnCraftChestStructure.class);
				if (eggs != null) {
					IFactoryEgg egg = eggs.get(new ItemMap((Inventory) (fccs
							.getChest().getState())));
					if (egg != null) {
						Factory f = egg.hatch(fccs, p);
						if (f != null) {
							addFactory(f);
						}
					}
				}
			}

		}
	}

	public void addFactoryEgg(Class<MultiBlockStructure> blockStructureClass,
			ItemMap recipe, IFactoryEgg egg) {
		HashMap<ItemMap, IFactoryEgg> eggs = factoryCreationRecipes
				.get(blockStructureClass);
		if (eggs == null) {
			eggs = new HashMap<ItemMap, IFactoryEgg>();
		}
		eggs.put(recipe, egg);
		this.eggs.put(egg.getName(), egg);
	}

	public boolean isPossibleCenterBlock(Material m) {
		return possibleCenterBlocks.contains(m);
	}

	public boolean isPossibleInteractionBlock(Material m) {
		return possibleInteractionBlock.contains(m);
	}

	public IFactoryEgg getEgg(String name) {
		return eggs.get(name);
	}
}
