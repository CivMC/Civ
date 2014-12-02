package vg.civcraft.mc.namelayer.permission;

import java.util.HashMap;
import java.util.Map;

import vg.civcraft.mc.namelayer.group.Group;

public class PermissionHandler {
	
	private Map<Group, GroupPermission> permissions = new HashMap<Group, GroupPermission>();
	
	public GroupPermission getGroupPermission(Group group){
		if (!permissions.containsKey(group))
			permissions.put(group, new GroupPermission(group));
		return permissions.get(group);
	}
	
	public void deletePerms(Group group){
		permissions.remove(group);
	}
}
