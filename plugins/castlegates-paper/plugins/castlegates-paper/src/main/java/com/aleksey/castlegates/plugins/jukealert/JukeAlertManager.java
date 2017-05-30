package com.aleksey.castlegates.plugins.jukealert;

import org.bukkit.Location;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.manager.SnitchManager;

public class JukeAlertManager implements IJukeAlertManager {
    private SnitchManager snitchManager = JukeAlert.getInstance().getSnitchManager();

	public IJukeAlert getJukeAlert(Location loc, int groupId) {
		return new com.aleksey.castlegates.plugins.jukealert.JukeAlert(snitchManager, loc, groupId);
	}
}
