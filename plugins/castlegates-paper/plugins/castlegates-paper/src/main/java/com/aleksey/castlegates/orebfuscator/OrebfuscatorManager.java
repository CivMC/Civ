package com.aleksey.castlegates.orebfuscator;

import java.util.List;

import org.bukkit.Location;

import com.lishid.orebfuscator.obfuscation.BlockUpdate;

public class OrebfuscatorManager implements IOrebfuscatorManager {
	public void update(List<Location> locations) {
		BlockUpdate.updateByLocations(locations, 1);
	}
}
