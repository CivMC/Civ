package com.aleksey.castlegates.plugins.jukealert;

import org.bukkit.Location;

public class NoJukeAlertManager implements IJukeAlertManager {
	public boolean hasJukeAlertAccess(Location location, String groupName) { return false; }
}
