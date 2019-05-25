package com.untamedears.JukeAlert;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.untamedears.JukeAlert.model.LoggingDelegate;
import com.untamedears.JukeAlert.model.Snitch;

import vg.civcraft.mc.namelayer.group.Group;

public class SnitchConfiguration {

	private int defaultRange;
	private long softDeleteTimer;
	private boolean triggerLevers;
	private long lifeTime;
	private long dormantLifeTime;
	private ItemStack item;
	private boolean displayOwnerOnBreak;

	public SnitchConfiguration(int defaultRange, long softDeleteTimer, boolean triggerLevers, long lifeTime,
			long dormantLifeTime, ItemStack item, boolean displayOwnerOnBreak) {
		this.defaultRange = defaultRange;
		this.softDeleteTimer = softDeleteTimer;
		this.triggerLevers = triggerLevers;
		this.lifeTime = lifeTime;
		this.dormantLifeTime = dormantLifeTime;
		this.item = item;
		this.displayOwnerOnBreak = displayOwnerOnBreak;
	}

	/**
	 * @return Default radius of the cuboid. Total length of the cuboid will be ((2
	 *         * defaultRange) + 1) in every dimension
	 */
	public int getDefaultRange() {
		return defaultRange;
	}

	/**
	 * @return How long snitch data is kept around for admin inspection after they
	 *         are destroyed
	 */
	public long getSoftDeleteTimer() {
		return softDeleteTimer;
	}

	/**
	 * @return Should snitches trigger adjacent levers
	 */
	public boolean shouldTriggerLevers() {
		return triggerLevers;
	}

	/**
	 * @return How long a snitch should stay alive until they go dormant, in milli
	 *         seconds
	 */
	public long getLifeTime() {
		return lifeTime;
	}

	/**
	 * @return How long snitches stay dormant after their life time runs out until
	 *         they entirely die, in milli seconds
	 */
	public long getDormantLifeTime() {
		return dormantLifeTime;
	}
	
	/**
	 * @return Item used to create instances of this snitch
	 */
	public ItemStack getItem() {
		return item;
	}
	
	
	/**
	 * @return Whether the snitch displays its group when broken 
	 */
	public boolean shouldDisplayOwnerOnBreak() {
		return displayOwnerOnBreak;
	}
	
	public Snitch createAt(Location loc, Player player, Group group) {
		LoggingDelegate delegate = new LoggingDelegate();
		Snitch snitch = new Snitch(loc, group, delegate, triggerLevers, defaultRange);
		return snitch;
	}

}
