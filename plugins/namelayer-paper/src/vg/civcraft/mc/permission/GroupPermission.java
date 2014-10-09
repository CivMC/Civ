package vg.civcraft.mc.permission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vg.civcraft.mc.GroupManager.PlayerType;
import vg.civcraft.mc.NameLayerPlugin;
import vg.civcraft.mc.database.GroupManagerDao;
import vg.civcraft.mc.group.Group;

public class GroupPermission {

	private Map<PlayerType, List<PermissionType>> perms;
	private GroupManagerDao db = NameLayerPlugin.getSaveManager();
	
	private Group group;
	public GroupPermission(Group group){
		this.group = group;
		loadPermsforGroup();
	}
	
	private void loadPermsforGroup(){
		perms = db.getPermissions(group.getName());
	}
	
	public boolean isAccessible(PermissionType type, PlayerType ptype){
		return perms.get(ptype).contains(type);
	}
	
	public String listPermsforPlayerType(PlayerType type){
		String x = "The permission types are: ";
		for (PermissionType pType: perms.get(type))
			x += pType.name() + " ";
		return x;
	}
	
	public boolean addPermission(PlayerType pType, PermissionType permType){
		if (perms.get(pType).contains(permType))
			return false;
		List<PermissionType> types = perms.get(pType);
		types.add(permType);
		String info = "";
		for (PermissionType t: types)
			info += t.name() + " ";
		db.updatePermissions(group.getName(), pType, info);
		return true;
	}
	
	public boolean removePermission(PlayerType pType, PermissionType permType){
		if (!perms.get(pType).contains(permType))
			return false;
		List<PermissionType> types = perms.get(pType);
		types.remove(permType);
		String info = "";
		for (PermissionType t: types)
			info += t.name() + " ";
		db.updatePermissions(group.getName(), pType, info);
		return true;
	}
	
	public PlayerType getFirstWithPerm(PermissionType type){
		for (PlayerType pType: perms.keySet()){
			if (perms.get(pType).contains(type))
				return pType;
		}
		return null;
	}
}
