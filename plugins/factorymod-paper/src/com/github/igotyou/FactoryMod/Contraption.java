package com.github.igotyou.FactoryMod;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.github.igotyou.FactoryMod.interactionManager.IInteractionManager;
import com.github.igotyou.FactoryMod.repairManager.IRepairManager;

public abstract class Contraption {
	protected IInteractionManager im;
	protected IRepairManager rm;
	protected Location physicalLocation;
	protected boolean active;

	public Contraption(Location loc, IInteractionManager im,
			IRepairManager rm) {
		this.physicalLocation = loc;
		this.im = im;
		this.rm = rm;
	}

	public IRepairManager getRepairManager() {
		return rm;
	}

	public IInteractionManager getInteractionManager() {
		return im;
	}
	
	public boolean isActive() {
		return active;
	}	

	public Location getLocation() {
		return physicalLocation;
	}

	public abstract boolean isWhole();

	public abstract void activate();

	public abstract void deactivate();
	
	public abstract void attemptToActivate(Player p);

}
