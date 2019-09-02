package com.untamedears.JukeAlert.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IgnoreList {

	// Only going to be used for /jamute when they mute all groups for a small amount of time

	private static Map<UUID, Boolean> playerIgnoreAlls = new HashMap<>();

	public static boolean doesPlayerIgnoreAll(UUID accountId) {

		synchronized (playerIgnoreAlls) {
			if (!playerIgnoreAlls.containsKey(accountId)) {
				return false;
			}
			return playerIgnoreAlls.get(accountId);
		}
	}

	// Toggle the global ignore flag for a specific account. If the player isn't
	//  noted in the map, enable the ignore bit
	public static boolean toggleIgnoreAll(UUID accountId) {

		boolean newState;
		synchronized (playerIgnoreAlls) {
			if (!playerIgnoreAlls.containsKey(accountId)) {
				newState = true;
			} else {
				newState = !playerIgnoreAlls.get(accountId);
			}
			playerIgnoreAlls.put(accountId, newState);
		}
		return newState;
	}

	private IgnoreList() { }
}
