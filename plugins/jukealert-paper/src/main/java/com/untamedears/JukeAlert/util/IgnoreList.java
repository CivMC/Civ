package com.untamedears.JukeAlert.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IgnoreList {

	// Only going to be used for /jamute when they mute all groups for a small amount of time

	private IgnoreList() { }

	private static Map<UUID, Boolean> playerIgnoreAlls_ = new HashMap<UUID, Boolean>();

	// Toggle the global ignore flag for a specific account. If the player isn't
	//  noted in the map, enable the ignore bit
	public static boolean toggleIgnoreAll(UUID accountId) {

		boolean newState;
		synchronized (playerIgnoreAlls_) {
			if (!playerIgnoreAlls_.containsKey(accountId)) {
				newState = true;
			} else {
				newState = !playerIgnoreAlls_.get(accountId);
			}
			playerIgnoreAlls_.put(accountId, newState);
		}
		return newState;
	}

	public static boolean doesPlayerIgnoreAll(UUID accountId) {

		synchronized (playerIgnoreAlls_) {
			if (!playerIgnoreAlls_.containsKey(accountId)) {
				return false;
			}
			return playerIgnoreAlls_.get(accountId);
		}
	}
}
