package com.github.civcraft.donum.inventories;

import java.util.Date;
import java.util.UUID;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;


public class DeathInventory extends AbstractInventoryStorage {
	
	private boolean returned;
	private Date deathTime;
	private int id;
	
	public DeathInventory(int id, UUID owner, ItemMap inventory, boolean returned, Date deathTime) {
		super(owner, inventory);
		this.returned = returned;
		this.id = id;
		this.deathTime = deathTime;
	}
	
	public boolean wasReturned() {
		return returned;
	}
	
	public void setReturned(boolean returned) {
		this.returned = returned;
	}
	
	public Date getDeathTime() {
		return deathTime;
	}
	
	public int getID() {
		return id;
	}

}
