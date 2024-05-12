package com.github.civcraft.donum.inventories;

import java.util.UUID;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;


public class DeliveryInventory extends AbstractInventoryStorage {
	
	private boolean dirty;

	public DeliveryInventory(UUID owner, ItemMap content) {
		super(owner, content);
		this.dirty = false;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public boolean isDirty() {
		return dirty;
	}
}