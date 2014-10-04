package com.untamedears.JukeAlert.group;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

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

    public List<String> getGroupsByAccount(UUID accountId, boolean includeFounders, boolean includeModerators, boolean includeMembers) {
    	List<String> returnValue = new ArrayList<String>();
    	if (includeFounders) {
    		for(Faction f : groupManager.getGroupsByFounder(accountId)) {
    			if (!returnValue.contains(f.getName())) {
    				returnValue.add(f.getName());
    			}
    		}
    	}
    	
    	if (includeModerators) {
    		for(Faction f : groupManager.getGroupsByModerator(accountId)) {
    			if (!returnValue.contains(f.getName())) {
    				returnValue.add(f.getName());
    			}
    		}
    	}
    	
    	if (includeMembers) {
    		for(Faction f : groupManager.getGroupsByMember(accountId)) {
    			if (!returnValue.contains(f.getName())) {
    				returnValue.add(f.getName());
    			}
    		}
    	}
    	
    	return returnValue;
    }
}
