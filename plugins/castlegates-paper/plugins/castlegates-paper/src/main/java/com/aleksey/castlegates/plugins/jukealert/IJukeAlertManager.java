package com.aleksey.castlegates.plugins.jukealert;

import com.aleksey.castlegates.plugins.citadel.ICitadel;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface IJukeAlertManager {
	boolean hasJukeAlertAccess(Location location, String groupName);
}