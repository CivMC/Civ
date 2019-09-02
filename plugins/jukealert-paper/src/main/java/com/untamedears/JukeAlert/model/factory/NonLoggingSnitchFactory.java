package com.untamedears.JukeAlert.model.factory;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.log.BroadCastingOnlyDelegate;
import com.untamedears.JukeAlert.model.log.LoggingDelegate;

import vg.civcraft.mc.civmodcore.util.ConfigParsing;
import vg.civcraft.mc.namelayer.group.Group;

public class NonLoggingSnitchFactory extends SnitchConfigFactory {
	
	private int defaultRange;
	private boolean triggerLevers;
	private long lifeTime;
	private long dormantLifeTime;
	private boolean displayOwnerOnBreak;

	@Override
	public boolean parse(ConfigurationSection config, Logger logger) {
		if (!super.parse(config, logger)) {
			return false;
		}
		defaultRange = config.getInt("range", 10);
		lifeTime = ConfigParsing.parseTime(config.getString("lifeTime", "2 weeks"), TimeUnit.MILLISECONDS);
		dormantLifeTime = ConfigParsing.parseTime(config.getString("dormantTime", "2 weeks"),
				TimeUnit.MILLISECONDS);
		displayOwnerOnBreak = config.getBoolean("displayOwnerOnBreak", true);
		triggerLevers = config.getBoolean("triggerLevers", true);
		return true;
	}
	


	/**
	 * @return Default radius of the cuboid. Total length of the cuboid will be ((2
	 *         * defaultRange) + 1) in every dimension
	 */
	public int getDefaultRange() {
		return defaultRange;
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
	 * @return Whether the snitch displays its group when broken 
	 */
	public boolean shouldDisplayOwnerOnBreak() {
		return displayOwnerOnBreak;
	}

	@Override
	public Snitch create(Location location, Group group, Player player) {
		LoggingDelegate logging = new BroadCastingOnlyDelegate();
		Snitch snitch = new Snitch(location, true, group.getGroupId(), logging, triggerLevers, defaultRange, id);
		logging.setSnitch(snitch);
		return snitch;
	}



	@Override
	public Snitch recreate(Location location, String name, int groupID, long lastRefresh, boolean canTriggerLevers) {
		LoggingDelegate logging = new BroadCastingOnlyDelegate();
		Snitch snitch = new Snitch(location, false, groupID, logging, triggerLevers, defaultRange, id);
		logging.setSnitch(snitch);
		return snitch;
	}

}
