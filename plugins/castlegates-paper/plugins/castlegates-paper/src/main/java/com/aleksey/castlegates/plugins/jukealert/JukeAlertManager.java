package com.aleksey.castlegates.plugins.jukealert;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.plugins.citadel.ICitadel;
import com.untamedears.JukeAlert.model.Snitch;
import org.bukkit.Location;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.manager.SnitchManager;

import java.util.Set;

public class JukeAlertManager implements IJukeAlertManager {
    private SnitchManager snitchManager = JukeAlert.getInstance().getSnitchManager();

	public boolean hasJukeAlertAccess(Location location, String groupName) {
		if(!CastleGates.getConfigManager().getInteractWithSnitches()) return false;

		Set<Snitch> snitches = this.snitchManager.findSnitches(location.getWorld(), location);

		if (snitches.size() > 0) {
			double distance = CastleGates.getCitadelManager().getMaxRedstoneDistance();

			for (Snitch snitch : snitches) {
				if (snitch.getGroup().getName().equalsIgnoreCase(groupName)
						&& snitch.shouldToggleLevers()
						&& snitch.getLoc().distance(location) <= distance
						)
				{
					return true;
				}
			}
		}

		return false;
	}
}
