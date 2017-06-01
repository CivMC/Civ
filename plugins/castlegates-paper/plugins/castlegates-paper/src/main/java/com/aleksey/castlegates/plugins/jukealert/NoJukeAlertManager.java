package com.aleksey.castlegates.plugins.jukealert;

import org.bukkit.Location;

public class NoJukeAlertManager implements IJukeAlertManager {
	public IJukeAlert getJukeAlert(Location loc, int groupId) { return new NoJukeAlert(); }
}
