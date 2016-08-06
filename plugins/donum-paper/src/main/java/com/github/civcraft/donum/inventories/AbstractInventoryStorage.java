package com.github.civcraft.donum.inventories;

import java.util.UUID;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class AbstractInventoryStorage {
	
	private UUID owner;
	protected ItemMap inventory;
	
	public AbstractInventoryStorage(UUID owner, ItemMap inventory) {
		this.owner = owner;
		this.inventory = inventory;
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public ItemMap getInventory() {
		return inventory;
	}

}
