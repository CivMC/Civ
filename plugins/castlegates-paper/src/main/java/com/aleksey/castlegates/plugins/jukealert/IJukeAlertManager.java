package com.aleksey.castlegates.plugins.jukealert;

import org.bukkit.Location;

public interface IJukeAlertManager {
	boolean hasJukeAlertAccess(Location location, String groupName);
}