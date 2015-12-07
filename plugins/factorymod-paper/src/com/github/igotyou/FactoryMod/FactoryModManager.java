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

public abstract class FactoryModManager {

	protected FactoryModPlugin plugin;
	private HashMap<Contraption, AFactoryProperties> contraptions = new HashMap<Contraption, AFactoryProperties>();
	private HashMap<Material, HashMap<Location, Contraption>> locations = new HashMap<Material, HashMap<Location, Contraption>>();
	private HashSet<Material> possibleCenterBlocks;
	private List<MultiBlockStructure> possibleStructures;

	public FactoryModManager(FactoryModPlugin plugin) {
		this.plugin = plugin;
		// Normal furnace, craftingtable, chest factories
		possibleStructures.add(new FurnCraftChestStructure());
		possibleCenterBlocks.add(Material.WORKBENCH);
	}

	public void addContraption(Contraption c, AFactoryProperties prop) {
		contraptions.put(c, prop);
	}

	public void removeContraption(Contraption c) {
		contraptions.remove(c);
	}

	public Contraption getFactoryAt(Location loc) {
		return getFactoryAt(loc.getBlock());
	}

	public Contraption getFactoryAt(Block b) {
		HashMap<Location, Contraption> forThisType = locations.get(b.getType());
		if (forThisType == null) {
			return null;
		} else {
			return forThisType.get(b.getLocation());
		}
	}

	public void addContraptionBlock(Block b, Contraption c) {
		HashMap<Location, Contraption> requiredMaterial = locations.get(b
				.getType());
		if (requiredMaterial == null) {
			requiredMaterial = new HashMap<Location, Contraption>();
		}
		requiredMaterial.put(b.getLocation(), c);
	}

	public boolean factoryExistsAt(Location loc) {
		return getFactoryAt(loc) == null;
	}

	public void attemptCreation(Block b, Player p) {
		if (possibleCenterBlocks.contains(b.getType())
				&& !factoryExistsAt(b.getLocation())) {
			for (MultiBlockStructure mbs : possibleStructures) {
				if (mbs.isComplete(b)) {
					Block invBlock = mbs.getInventoryBlock(b);
					if (invBlock != null) {
						Inventory inv = ((InventoryHolder) (invBlock.getState()))
								.getInventory();
						//ItemMap here
					}
				}
			}

		}
	}
}
