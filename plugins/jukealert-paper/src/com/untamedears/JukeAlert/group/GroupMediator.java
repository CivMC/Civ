package com.untamedears.JukeAlert.group;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class GroupMediator {

    private GroupManager groupManager;

    public GroupMediator() {
        this.groupManager = NameAPI.getGroupManager();
    }

    public Group getGroupByName(String groupName) {
        return groupManager.getGroup(groupName);
    }

    public List<String> getGroupsByAccount(UUID accountId, boolean includeFounders, boolean includeModerators, boolean includeMembers) {
    	List<String> returnValue = new ArrayList<String>();
    		List<Group> groups = new ArrayList<Group>();
    		for (String group: groupManager.getAllGroupNames(accountId)){
    			Group g = groupManager.getGroup(group);
    			if (includeFounders && g.isOwner(accountId))
    				groups.add(g);
    			GroupPermission perm = groupManager.getPermissionforGroup(g);
    			if (includeModerators && perm.isAccessible(g.getPlayerType(accountId), PermissionType.BLOCKS))
    				groups.add(g);
    			if (includeMembers && g.isMember(accountId))
    				groups.add(g);
    		}
    		for(Group f : groups) {
    			if (!returnValue.contains(f.getName())) {
    				returnValue.add(f.getName());
    			}
    		}
    	
    	return returnValue;
    }
}
