package com.untamedears.JukeAlert.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class IgnoreList {
	//only going to be used for /jamute when they mute all groups for a small amount of time
	
	private IgnoreList() { }

	//private static final Object lockObject_ = new Object();
	//private static Map<UUID, Set<String>> ignoresByPlayer_ = new HashMap<UUID, Set<String>>();
	//private static Map<String, Set<UUID>> ignoresByGroup_ = new HashMap<String, Set<UUID>>();
	private static Map<UUID, Boolean> playerIgnoreAlls_ = new HashMap<UUID, Boolean>();

/*	// Flip the ignore/unignore bit for a player-group combination.
	// Requires quick access with lookup by player.
	// Returns true if it adds to ignore list.
	public static Boolean toggleIgnore(UUID accountId, String groupName) {
		
		Boolean returnValue = false;
		groupName = groupName.toLowerCase();
		
		synchronized(lockObject_) {

			// If the player has no ignores, this is obviously an add and will have
			//	an ignore after this call is over.  Add a list for us to append to.
			if (!ignoresByPlayer_.containsKey(accountId)) {
				ignoresByPlayer_.put(accountId, new HashSet<String>());
			}
			
			// If the group has no ignores, this is obviously an add and will have
			//	an ignore after this call is over.  Add a list for us to append to.
			if (!ignoresByGroup_.containsKey(groupName)) {
				ignoresByGroup_.put(groupName, new HashSet<UUID>());
			}
			
			Set<String> playerIgnores = ignoresByPlayer_.get(accountId);
			
			// If the player has this group name ignored:
			//	Remove it from the player reference.
			// 	Remove the player from the group reference.
			//	If possible, remove the group from the group reference.
			//	If possible, remove the player from the player reference.
			if (playerIgnores.contains(groupName)) {
				
				// Remove from player reference.
				playerIgnores.remove(groupName);
				
				// Remove the player from the group reference.
				ignoresByGroup_.get(groupName).remove(accountId);
				
				// Remove group if we can.
				if (ignoresByGroup_.get(groupName).size() == 0) {
					ignoresByGroup_.remove(groupName);
				}
				
				// Remove player if we can.
				if (playerIgnores.size() == 0) {
					ignoresByPlayer_.remove(accountId);
				}
				
			} 
			// Else if it does not contain the group name, lets add it in.
			else {
				playerIgnores.add(groupName);
				ignoresByGroup_.get(groupName).add(accountId);
				returnValue = true;
			}
			
		}
		
		return returnValue;
	}*/
	
	//Not used due to ignored groups being in db now
	
	/*// Obtain list of all player ignores for a given group.
	// Requires quick accesss by group name.
	// !!IMPORTANT!!:  Returns null if there exists no player ignores for a given group.
	// !!IMPORTANT!!:  This will return a reference value for a set for this group for speed reasons.
	//	Folks accessing this method would be well advised to keep this in mind.	
	public static Set<UUID> GetPlayerIgnoreListByGroup(String groupName) {
		
		groupName = groupName.toLowerCase();
		Set<UUID> returnValue;
		
		synchronized(lockObject_) {
            returnValue = ignoresByGroup_.get(groupName);
		}
		
		return returnValue;
	}*/
	
/*	// Obtain list of all group ignores for an account.
	// Requires quick accesss by account ID
	// !!IMPORTANT!!:  Returns null if there exists no group ignores for a given player.
	// !!IMPORTANT!!:  This will return a reference value for a set for this group for speed reasons.
	//	Folks accessing this method would be well advised to keep this in mind.	
	public static Set<String> GetGroupIgnoreListByPlayer(UUID accountId) {
		
		Set<String> returnValue;
		
		synchronized(lockObject_) {
            returnValue = ignoresByPlayer_.get(accountId);
		}
		
		return returnValue;
	}*/

    // Toggle the global ignore flag for a specific account. If the player isn't
    // noted in the map, enable the ignore bit.
    public static boolean toggleIgnoreAll(UUID accountId) {
        boolean newState;
        synchronized(playerIgnoreAlls_) {
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
        synchronized(playerIgnoreAlls_) {
            if (!playerIgnoreAlls_.containsKey(accountId)) {
                return false;
            }
            return playerIgnoreAlls_.get(accountId);
        }
    }
}
