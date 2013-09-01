package com.untamedears.JukeAlert.group;

import java.util.List;
import java.util.ArrayList;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
import com.untamedears.citadel.entity.Faction;

public class GroupMediator {

    GroupManager groupManager;

    public GroupMediator() {
        this.groupManager = Citadel.getGroupManager();
    }

    public Faction getGroupByName(String groupName) {
        return groupManager.getGroup(groupName);
    }
    
    public List<String> getGroupsByPlayerName(String playerName, boolean includeFounders, boolean includeModerators, boolean includeMembers) {
    	List<String> returnValue = new ArrayList<String>();
    	
    	if (includeFounders) {
    		for(Faction f : groupManager.getGroupsByFounder(playerName)) {
    			if (!returnValue.contains(f.getName())) {
    				returnValue.add(f.getName());
    			}
    		}
    	}
    	
    	if (includeModerators) {
    		for(Faction f : groupManager.getGroupsByModerator(playerName)) {
    			if (!returnValue.contains(f.getName())) {
    				returnValue.add(f.getName());
    			}
    		}
    	}
    	
    	if (includeMembers) {
    		for(Faction f : groupManager.getGroupsByMember(playerName)) {
    			if (!returnValue.contains(f.getName())) {
    				returnValue.add(f.getName());
    			}
    		}
    	}
    	
    	return returnValue;
    }
}
