package com.untamedears.JukeAlert.model.factory;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.log.BroadCastingOnlyDelegate;
import com.untamedears.JukeAlert.model.log.FullyLoggingDelegate;
import com.untamedears.JukeAlert.model.log.LoggingDelegate;

import vg.civcraft.mc.civmodcore.util.ConfigParsing;
import vg.civcraft.mc.namelayer.group.Group;

public class LoggingSnitchFactory extends NonLoggingSnitchFactory {

	private long softDeleteTimer;
	private long logKeepingTime;

	@Override
	public boolean parse(ConfigurationSection config, Logger logger) {
		if (!super.parse(config, logger)) {
			return false;
		}
		softDeleteTimer = ConfigParsing.parseTime(config.getString("softDeleteTimer", "2 weeks"),
				TimeUnit.MILLISECONDS);
		logKeepingTime = ConfigParsing.parseTime(config.getString("logKeepingTime", "2 weeks"), TimeUnit.MILLISECONDS);
		return true;
	}

	@Override
	public Snitch create(Location location, Group group, Player player) {
		LoggingDelegate logging = new FullyLoggingDelegate();
		Snitch snitch = new Snitch(location, true, group.getGroupId(), logging, shouldTriggerLevers(),
				getDefaultRange(), getID());
		logging.setSnitch(snitch);
		return snitch;
	}
	
	@Override
	public Snitch recreate(Location location, String name, int groupID, long lastRefresh, boolean canTriggerLevers) {
		LoggingDelegate logging = new BroadCastingOnlyDelegate();
		Snitch snitch = new Snitch(location, false, groupID, logging, canTriggerLevers, getDefaultRange(), id);
		logging.setSnitch(snitch);
		return snitch;
	}

	/**
	 * @return How long snitch data is kept around for admin inspection after they
	 *         are destroyed
	 */
	public long getSoftDeleteTimer() {
		return softDeleteTimer;
	}

	/**
	 * @return How long should logs be kept before being deleted
	 */
	public long getLogKeepingTime() {
		return logKeepingTime;
	}

}
