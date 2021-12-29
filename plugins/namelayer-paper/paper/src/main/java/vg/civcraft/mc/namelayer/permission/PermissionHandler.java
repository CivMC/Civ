package vg.civcraft.mc.namelayer.permission;

import java.util.HashMap;
import java.util.Map;
import vg.civcraft.mc.namelayer.group.Group;

public class PermissionHandler {
	
	private Map<Group, GroupPermission> permissions = new HashMap<Group, GroupPermission>();
	/**
	 * Gets the specific GroupPermission for a Group.
	 * @param group- The group.
	 * @return Returns the GroupPermission.
	 */
	public GroupPermission getGroupPermission(Group group){
		if (!permissions.containsKey(group))
			permissions.put(group, new GroupPermission(group));
		return permissions.get(group);
	}
	/**
	 * Deletes a Group's perms, should only be used if deleting a group.
	 * @param group- The Group that was deleted.
	 */
	public void deletePerms(Group group){
		permissions.remove(group);
	}
}
