package com.aleksey.castlegates.jukealert;

import org.bukkit.Location;

public class NoJukeAlertManager implements IJukeAlertManager {
	public boolean hasToggleLeverSnitchInRadius(Location loc, int groupId) {
		return false;
	}
}
