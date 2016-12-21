package com.untamedears.JukeAlert.group;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class GroupMediator {

    private GroupManager groupManager;

    public GroupMediator() {
        this.groupManager = NameAPI.getGroupManager();
    }

    public Group getGroupByName(String groupName) {
        return GroupManager.getGroup(groupName);
    }

    public List<String> getGroupsWithPermission(UUID accountId, PermissionType perm) {
    	List<String> returnValue = new ArrayList<String>();
		List<Group> groups = new ArrayList<Group>();
		for (String group: groupManager.getAllGroupNames(accountId)){
			Group g = GroupManager.getGroup(group);
			if (g == null) {
				continue;
			}
			if (groupManager.hasAccess(group, accountId, perm)) {
				groups.add(g);
			}
		}
		for(Group f : groups) {
			if (!returnValue.contains(f.getName())) {
				returnValue.add(f.getName());
			}
		}
    	
    	return returnValue;
    }
}
