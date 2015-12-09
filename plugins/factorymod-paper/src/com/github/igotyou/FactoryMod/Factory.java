package com.github.igotyou.FactoryMod;

import org.bukkit.entity.Player;

import com.github.igotyou.FactoryMod.interactionManager.IInteractionManager;
import com.github.igotyou.FactoryMod.multiBlockStructures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.powerManager.IPowerManager;
import com.github.igotyou.FactoryMod.repairManager.IRepairManager;

public abstract class Factory implements Runnable{
	protected IInteractionManager im;
	protected IRepairManager rm;
	protected IPowerManager pm;
	protected boolean active;
	protected MultiBlockStructure mbs;
	protected int updateTime;

	public Factory(IInteractionManager im, IRepairManager rm,
			IPowerManager pm, MultiBlockStructure mbs, int updateTime) {
		this.im = im;
		this.rm = rm;
		this.mbs = mbs;
		this.pm = pm;
		this.updateTime = updateTime;
	}

	public IRepairManager getRepairManager() {
		return rm;
	}

	public IInteractionManager getInteractionManager() {
		return im;
	}

	public IPowerManager getPowerManager() {
		return pm;
	}

	public boolean isActive() {
		return active;
	}

	public MultiBlockStructure getMultiBlockStructure() {
		return mbs;
	}

	public int getUpdateTime() {
		return updateTime;
	}

	public abstract void activate();

	public abstract void deactivate();

	public abstract void attemptToActivate(Player p);

}
