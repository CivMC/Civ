package com.valadian.nametracker;

import java.util.Map;
import java.util.UUID;

public class NameAPI {
	public static UUID getUUID(String playerName) {
		return NameTrackerPlugin.associations.getUUID(playerName);
	}
	
	public static String getCurrentName(UUID uuid) {
		return NameTrackerPlugin.associations.getCurrentName(uuid);
	}
	
	public static Map<UUID, String> getAllAccounts(){
		return NameTrackerPlugin.associations.getAllUUIDSNames();
	}
}
