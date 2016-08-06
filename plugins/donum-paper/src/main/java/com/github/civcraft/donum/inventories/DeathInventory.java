package com.github.civcraft.donum.inventories;

import java.util.Date;
import java.util.UUID;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class DeathInventory extends AbstractInventoryStorage {
	
	private boolean returned;
	private Date deathTime;
	
	public DeathInventory(UUID owner, ItemMap inventory, boolean returned, Date deathTime) {
		super(owner, inventory);
		this.returned = returned;
		this.deathTime = deathTime;
	}
	
	public boolean wasReturned() {
		return returned;
	}
	
	public Date getDeathTime() {
		return deathTime;
	}

}
