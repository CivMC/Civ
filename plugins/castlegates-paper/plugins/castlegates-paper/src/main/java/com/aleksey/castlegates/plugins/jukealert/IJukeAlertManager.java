package com.aleksey.castlegates.plugins.jukealert;

import org.bukkit.Location;

public interface IJukeAlertManager {
	IJukeAlert getJukeAlert(Location loc, int groupId);
}
