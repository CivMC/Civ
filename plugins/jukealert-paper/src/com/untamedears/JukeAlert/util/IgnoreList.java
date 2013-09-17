package com.untamedears.JukeAlert.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IgnoreList {
	
	
	private IgnoreList() { }

	private static final Object lockObject_ = new Object();
	private static Map<String, Set<String>> ignoresByPlayer_ = new HashMap<String, Set<String>>();
	private static Map<String, Set<String>> ignoresByGroup_ = new HashMap<String, Set<String>>();
	
	public static void removeIgnoresForPlayer(String playerName) {

		playerName = playerName.toLowerCase();
		
		// If a player has any ignores, get the ignore list for the players.
		// For each group listed as a player ignore, remove the player from this ignore group list.
		// For each group we modify, check to see if the group is now empty.  If so, remove it from the list.
		// After removing the player from all ignoresByGroup lists, remove the ignore by player reference.
		synchronized(lockObject_) {
			if (ignoresByPlayer_.containsKey(playerName)) {
				for(String group : ignoresByPlayer_.get(playerName)) {
					if (ignoresByGroup_.containsKey(group)) {
						
						ignoresByGroup_.get(group).remove(playerName);

						// Remove group if we can.
						if (ignoresByGroup_.get(group).size() == 0) {
							ignoresByGroup_.remove(group);
						}
					}
				}
				ignoresByPlayer_.remove(playerName);
			}
		}
	}
	
	// Flip the ignore/unignore bit for a player-group combination.
	// Requires quick access with lookup by player.
	// Returns true if it adds to ignore list.
	public static Boolean toggleIgnore(String playerName, String groupName) {
		
		Boolean returnValue = false;
		playerName = playerName.toLowerCase();
		groupName = groupName.toLowerCase();
		
		synchronized(lockObject_) {

			// If the player has no ignores, this is obviously an add and will have
			//	an ignore after this call is over.  Add a list for us to append to.
			if (!ignoresByPlayer_.containsKey(playerName)) {
				ignoresByPlayer_.put(playerName, new HashSet<String>());
			}
			
			// If the group has no ignores, this is obviously an add and will have
			//	an ignore after this call is over.  Add a list for us to append to.
			if (!ignoresByGroup_.containsKey(groupName)) {
				ignoresByGroup_.put(groupName, new HashSet<String>());
			}
			
			Set<String> playerIgnores = ignoresByPlayer_.get(playerName);
			
			// If the player has this group name ignored:
			//	Remove it from the player reference.
			// 	Remove the player from the group reference.
			//	If possible, remove the group from the group reference.
			//	If possible, remove the player from the player reference.
			if (playerIgnores.contains(groupName)) {
				
				// Remove from player reference.
				playerIgnores.remove(groupName);
				
				// Remove the player from the group reference.
				ignoresByGroup_.get(groupName).remove(playerName);
				
				// Remove group if we can.
				if (ignoresByGroup_.get(groupName).size() == 0) {
					ignoresByGroup_.remove(groupName);
				}
				
				// Remove player if we can.
				if (playerIgnores.size() == 0) {
					ignoresByPlayer_.remove(playerName);
				}
				
			} 
			// Else if it does not contain the group name, lets add it in.
			else {
				playerIgnores.add(groupName);
				ignoresByGroup_.get(groupName).add(playerName);
				returnValue = true;
			}
			
		}
		
		return returnValue;
	}
	
	// Obtain list of all player ignores for a given group.
	// Requires quick accesss by group name.
	// !!IMPORTANT!!:  Returns null if there exists no player ignores for a given group.
	// !!IMPORTANT!!:  This will return a reference value for a set for this group for speed reasons.
	//	Folks accessing this method would be well advised to keep this in mind.	
	public static Set<String> GetPlayerIgnoreListByGroup(String groupName) {
		
		groupName = groupName.toLowerCase();
		Set<String> returnValue = null;
		
		synchronized(lockObject_) {
			if(ignoresByGroup_.containsKey(groupName)) {
				returnValue = ignoresByGroup_.get(groupName);
			}
		}
		
		return returnValue;
	}
	
	// Obtain list of all group ignores for a playername.
	// Requires quick accesss by player name
	// !!IMPORTANT!!:  Returns null if there exists no group ignores for a given player.
	// !!IMPORTANT!!:  This will return a reference value for a set for this group for speed reasons.
	//	Folks accessing this method would be well advised to keep this in mind.	
	public static Set<String> GetGroupIgnoreListByPlayer(String playerName) {
		
		playerName = playerName.toLowerCase();
		
		Set<String> returnValue = null;
		
		synchronized(lockObject_) {
			if(ignoresByPlayer_.containsKey(playerName)) {
				returnValue = ignoresByPlayer_.get(playerName);
			}
		}
		
		return returnValue;
	}
}
