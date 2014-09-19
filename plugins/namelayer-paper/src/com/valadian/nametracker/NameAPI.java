package com.valadian.nametracker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NameAPI {
	private static Map<UUID, String> ucache = new HashMap<UUID, String>();
	private static Map<String, UUID> ncache = new HashMap<String, UUID>();
	
	public static UUID getUUID(String playerName) {
		if (!ncache.containsKey(playerName))
			ncache.put(playerName,  NameTrackerPlugin.associations.getUUID(playerName));
		return ncache.get(playerName);
	}
	
	public static String getCurrentName(UUID uuid) {
		if (!ucache.containsKey(uuid))
			ucache.put(uuid, NameTrackerPlugin.associations.getCurrentName(uuid));
		return ucache.get(uuid);
	}
	
	public static Map<UUID, String> getAllAccounts(){
		return NameTrackerPlugin.associations.getAllUUIDSNames();
	}
}
