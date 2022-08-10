package com.aleksey.castlegates.plugins.jukealert;

import com.aleksey.castlegates.CastleGates;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.appender.LeverToggleAppender;
import org.bukkit.Location;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.SnitchManager;

import java.util.Set;

public class JukeAlertManager implements IJukeAlertManager {
    private final SnitchManager _snitchManager = JukeAlert.getInstance().getSnitchManager();

	public boolean hasJukeAlertAccess(Location location, String groupName) {
		if(!CastleGates.getConfigManager().getInteractWithSnitches()) return false;

		Set<Snitch> snitches = _snitchManager.getSnitchesCovering(location);

		if (snitches.size() > 0) {
			double distance = CastleGates.getCitadelManager().getMaxRedstoneDistance();

			for (Snitch snitch : snitches) {
				if (snitch.getGroup().getName().equalsIgnoreCase(groupName)
						&& snitch.getLocation().distance(location) <= distance
						)
				{
					LeverToggleAppender toggleAppender = snitch.getAppender(LeverToggleAppender.class);
					if (toggleAppender == null) {
						return false;
					}
					if (toggleAppender.shouldToggle())
						return true;
				}
			}
		}

		return false;
	}
}
