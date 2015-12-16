package com.github.igotyou.FactoryMod;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.multiBlockStructures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.multiBlockStructures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.utility.ItemMap;

/**
 * Manager class which handles all factories, their locations and their creation
 *
 */
public class FactoryModManager {
	protected FactoryModPlugin plugin;
	private HashMap<Class<MultiBlockStructure>, HashMap<ItemMap, IFactoryEgg>> factoryCreationRecipes;
	private HashMap<Location, Factory> locations;
	private HashMap<String, IFactoryEgg> eggs;
	private HashMap<String, HashMap<ItemMap, IFactoryEgg>> upgradeRecipes;
	private HashSet<Factory> factories;
	private HashSet<Material> possibleCenterBlocks;
	private HashSet<Material> possibleInteractionBlock;
	private Material factoryInteractionMaterial;
	private boolean citadelEnabled;
	private int redstonePowerOn;
	private int redstoneRecipeChange;
	private String compactLore;

	public FactoryModManager(FactoryModPlugin plugin,
			Material factoryInteractionMaterial, boolean citadelEnabled, int redstonePowerOn, int redstoneRecipeChange) {
		this.plugin = plugin;
		this.factoryInteractionMaterial = factoryInteractionMaterial;
		this.citadelEnabled = citadelEnabled;
		this.redstonePowerOn = redstonePowerOn;
		this.redstoneRecipeChange = redstoneRecipeChange;

		factoryCreationRecipes = new HashMap<Class<MultiBlockStructure>, HashMap<ItemMap, IFactoryEgg>>();
		locations = new HashMap<Location, Factory>();
		eggs = new HashMap<String, IFactoryEgg>();
		upgradeRecipes = new HashMap<String, HashMap<ItemMap, IFactoryEgg>>();
		possibleCenterBlocks = new HashSet<Material>();
		possibleInteractionBlock = new HashSet<Material>();
		factories = new HashSet<Factory>();

		// Normal furnace, craftingtable, chest factories
		possibleCenterBlocks.add(Material.WORKBENCH);
		possibleInteractionBlock.add(Material.WORKBENCH);
		possibleInteractionBlock.add(Material.FURNACE);
		possibleInteractionBlock.add(Material.BURNING_FURNACE);
		possibleInteractionBlock.add(Material.CHEST);
	}

	/**
	 * Sets the lore used for compacting recipes. This is needed for the compact
	 * item listeners
	 * 
	 * @param lore
	 *            Lore used for compacting items
	 */
	public void setCompactLore(String lore) {
		compactLore = lore;
	}

	/**
	 * @return Lore given to compacted items
	 */
	public String getCompactLore() {
		return compactLore;
	}

	/**
	 * Adds a factory and the locations of its blocks to the manager
	 * 
	 * @param f
	 *            Factory to add
	 */
	public void addFactory(Factory f) {
		factories.add(f);
		for (Block b : f.getMultiBlockStructure().getAllBlocks()) {
			locations.put(b.getLocation(), f);
		}
	}

	/**
	 * @return Whether citadel is enabled on the server
	 */
	public boolean isCitadelEnabled() {
		return citadelEnabled;
	}

	/**
	 * @return Which material is used to interact with factories, stick by
	 *         default
	 */
	public Material getFactoryInteractionMaterial() {
		return factoryInteractionMaterial;
	}

	/**
	 * Removes a factory from the manager
	 * 
	 * @param f
	 *            Factory to remove
	 */
	public void removeFactory(Factory f) {
		factories.remove(f);
		for (Block b : f.getMultiBlockStructure().getAllBlocks()) {
			locations.remove(b.getLocation());
		}
	}

	/**
	 * Tries to get the factory which has a part at the given location
	 * 
	 * @param loc
	 *            Location which is supposed to be part of a factory
	 * @return The factory which had a block at the given location or null if
	 *         there was no factory
	 */
	public Factory getFactoryAt(Location loc) {
		return getFactoryAt(loc.getBlock());
	}

	/**
	 * Tries to get the factory which has a part at the given block
	 * 
	 * @param b
	 *            Block which is supposed to be part of a factory
	 * @return The factory which had a block at the given location or null if
	 *         there was no factory
	 */
	public Factory getFactoryAt(Block b) {
		return locations.get(b.getLocation());
	}

	/**
	 * Checks whether a part of a factory is at the given location
	 * 
	 * @param loc
	 *            Location to check
	 * @return True if there is a factory block, false if not
	 */
	public boolean factoryExistsAt(Location loc) {
		return getFactoryAt(loc) != null;
	}

	/**
	 * Attempts to create a factory with the given block as new center block. If
	 * all blocks for a specific structure are there and other conditions needed
	 * for the factory type are fullfilled, the factory is created and added to
	 * the manager
	 * 
	 * @param b
	 *            Center block
	 * @param p
	 *            Player attempting to create the factory
	 */
	public void attemptCreation(Block b, Player p) {
		if (!factoryExistsAt(b.getLocation())) {
			// Cycle through possible structures here
			FurnCraftChestStructure fccs = new FurnCraftChestStructure(b);
			if (fccs.isComplete()) {
				HashMap<ItemMap, IFactoryEgg> eggs = factoryCreationRecipes
						.get(FurnCraftChestStructure.class);
				if (eggs != null) {
					IFactoryEgg egg = eggs.get(new ItemMap(((Chest) (fccs
							.getChest().getState())).getInventory()));
					if (egg != null) {
						Factory f = egg.hatch(fccs, p);
						if (f != null) {
							((Chest) (fccs.getChest().getState()))
									.getInventory().clear();
							addFactory(f);
							p.sendMessage(ChatColor.GREEN
									+ "Successfully created " + f.getName());
						}
					} else {
						p.sendMessage(ChatColor.RED
								+ "There is no factory with the given creation materials");
					}
				}
			} else {
				p.sendMessage(ChatColor.RED + "This is no complete factory");
			}

		}
	}

	/**
	 * Adds a factory egg to the manager and associates it with a specific setup
	 * cost in items and a specific MultiBlockStructure which is the physical
	 * representation of the factory created by the egg. See the docu for the
	 * eggs for more info on those
	 * 
	 * @param blockStructureClass
	 *            Class inheriting from MultiBlockStructure, which physically
	 *            represents the factories created by the egg
	 * @param recipe
	 *            Item cost to create the factory
	 * @param egg
	 *            Encapsulates the factory itself
	 */
	public void addFactoryCreationEgg(Class blockStructureClass,
			ItemMap recipe, IFactoryEgg egg) {
		HashMap<ItemMap, IFactoryEgg> eggs = factoryCreationRecipes
				.get(blockStructureClass);
		if (eggs == null) {
			eggs = new HashMap<ItemMap, IFactoryEgg>();
			factoryCreationRecipes.put(blockStructureClass, eggs);
		}
		eggs.put(recipe, egg);
		this.eggs.put(egg.getName(), egg);
	}

	public void addFactoryUpgradeEgg(String factoryUpgradingFrom, ItemMap cost,
			IFactoryEgg egg) {
		HashMap<ItemMap, IFactoryEgg> eggs = upgradeRecipes
				.get(factoryUpgradingFrom);
		if (eggs == null) {
			eggs = new HashMap<ItemMap, IFactoryEgg>();
			upgradeRecipes.put(factoryUpgradingFrom, eggs);
		}
		eggs.put(cost, egg);
		this.eggs.put(egg.getName(), egg);
	}

	/**
	 * Checks whether a specific material is a possible center block for a
	 * factory and whether a factory could potentionally created from a block
	 * with this material
	 * 
	 * @param m
	 *            Material to check
	 * @return true if the material could be the one of a possible center block,
	 *         false if not
	 */
	public boolean isPossibleCenterBlock(Material m) {
		return possibleCenterBlocks.contains(m);
	}

	/**
	 * Checks whether the given material is an interaction material and whether
	 * a reaction should be tried to get when one of those blocks is part of a
	 * factory and interacted with
	 * 
	 * @param m
	 *            Material to check
	 * @return True if the material is a possible interaction material, false if
	 *         not
	 */
	public boolean isPossibleInteractionBlock(Material m) {
		return possibleInteractionBlock.contains(m);
	}

	/**
	 * Gets a specific factory egg based on it's name
	 * 
	 * @param name
	 *            Name of the egg
	 * @return The egg with the given name or null if no such egg exists
	 */
	public IFactoryEgg getEgg(String name) {
		return eggs.get(name);
	}
	
	/**
	 * Gets the Redstone power level necessary to active a factory.
	 * Fall below this level and the factory will deactivate.
	 *  
	 * @return The power level on which factory activation or de-activation hinges
	 */
	public int getRedstonePowerOn() {
		return this.redstonePowerOn;
	}
	
	/**
	 * Gets the Redstone power change necessary to alter the recipe setting of a factory.
	 * Any change >= this level, either positive or negative, will attempt to alter the recipe 
	 *   (implementation depending).
	 *   
	 * @return The amount of Redstone power change necessary to alter recipe setting of a factory.
	 */
	public int getRedstoneRecipeChange() {
		return this.redstoneRecipeChange;
	}
}
