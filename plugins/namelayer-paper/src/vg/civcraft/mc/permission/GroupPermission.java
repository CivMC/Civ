package vg.civcraft.mc.permission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vg.civcraft.mc.GroupManager.PlayerType;
import vg.civcraft.mc.NameTrackerPlugin;
import vg.civcraft.mc.database.GroupManagerDao;
import vg.civcraft.mc.group.Group;

public class GroupPermission {

	private Map<PlayerType, List<PermissionType>> perms;
	private GroupManagerDao db = NameTrackerPlugin.getSaveManager();
	
	private Group group;
	public GroupPermission(Group group){
		this.group = group;
		loadPermsforGroup();
	}
	
	public void loadPermsforGroup(){
		perms = db.getPermissions(group.getName());
	}
	
	public boolean isAccessible(PermissionType type, PlayerType ptype){
		return perms.get(ptype).contains(type);
	}
	
	public void 
}
