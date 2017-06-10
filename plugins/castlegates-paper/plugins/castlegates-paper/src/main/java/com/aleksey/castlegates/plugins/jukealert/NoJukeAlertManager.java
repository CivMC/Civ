package com.aleksey.castlegates.plugins.jukealert;

import com.aleksey.castlegates.plugins.citadel.ICitadel;
import org.bukkit.Location;

public class NoJukeAlertManager implements IJukeAlertManager {
	public boolean hasJukeAlertAccess(Location location, int groupId) { return false; }
}
