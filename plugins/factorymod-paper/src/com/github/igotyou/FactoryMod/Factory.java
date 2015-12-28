package com.github.igotyou.FactoryMod;

import org.bukkit.entity.Player;

import com.github.igotyou.FactoryMod.interactionManager.IInteractionManager;
import com.github.igotyou.FactoryMod.multiBlockStructures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.powerManager.IPowerManager;
import com.github.igotyou.FactoryMod.repairManager.IRepairManager;

/**
 * Super class for any sort of factory created by this plugin
 *
 */
public abstract class Factory implements Runnable {
	protected IInteractionManager im;
	protected IRepairManager rm;
	protected IPowerManager pm;
	protected boolean active;
	protected MultiBlockStructure mbs;
	protected int updateTime;
	protected String name;

	public Factory(IInteractionManager im, IRepairManager rm, IPowerManager pm,
			MultiBlockStructure mbs, int updateTime, String name) {
		this.im = im;
		this.rm = rm;
		this.mbs = mbs;
		this.pm = pm;
		this.updateTime = updateTime;
		this.name = name;
	}

	/**
	 * @return The manager which handles health, repairs and decay of the
	 *         factory
	 */
	public IRepairManager getRepairManager() {
		return rm;
	}

	/**
	 * @return The manager which handles any sort of player interaction with the
	 *         factory
	 */
	public IInteractionManager getInteractionManager() {
		return im;
	}

	/**
	 * @return The manager which handles power and it's consumption for this
	 *         factory
	 */
	public IPowerManager getPowerManager() {
		return pm;
	}

	/**
	 * @return Whether this factory is currently turned on
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @return The physical structure representing this factory
	 */
	public MultiBlockStructure getMultiBlockStructure() {
		return mbs;
	}

	/**
	 * @return How often this factory is updated when it's turned on, measured
	 *         in ticks
	 */
	public int getUpdateTime() {
		return updateTime;
	}

	/**
	 * Names are not unique for factory instances, but simply describe a broader
	 * functionality group. Factories implemented by the same class can have
	 * different names, but factories with the same name should have the exact
	 * same functionality
	 * 
	 * @return name of this factory
	 */
	public String getName() {
		return name;
	}

	/**
	 * Activates this factory
	 */
	public abstract void activate();

	/**
	 * Deactivates this factory
	 */
	public abstract void deactivate();

	/**
	 * Attempts to turn this factory on and does any checks needed
	 * 
	 * @param p
	 *            Player turning the factory on or null if something other than
	 *            a player is attempting to turn it on
	 */
	public abstract void attemptToActivate(Player p);

	/**
	 * Gets all the information needed to recreate this factory at a later
	 * point. The implementation of this method should be in line with the egg
	 * of the factory type to ensure persistence past restarts without problems
	 * 
	 * @return Everything needed to recreate this factory
	 */
	public abstract String serialize();

}
