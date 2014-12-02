package vg.civcraft.mc.namelayer.permission;

import java.util.List;
import java.util.Map;

import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.group.groups.PublicGroup;

public class GroupPermission {

	private Map<PlayerType, List<PermissionType>> perms;
	private GroupManagerDao db = NameLayerPlugin.getGroupManagerDao();
	
	private Group group;
	public GroupPermission(Group group){
		this.group = group;
		loadPermsforGroup();
	}
	
	private void loadPermsforGroup(){
		perms = db.getPermissions(group.getName());
	}
	
	public boolean isAccessible(PlayerType ptype, PermissionType... type){
		if (group instanceof PublicGroup){
			switch (type[0]){
			case DOORS:
			case CHESTS:
				return true;
			default:;
			}
		}
		boolean hasPerm = false;
		for (PermissionType t: type)
			if (perms.get(ptype).contains(t))
				hasPerm = true;
		return hasPerm;
	}
	
	public boolean hasAllPermissions(PlayerType ptype, PermissionType... type){
		for (PermissionType t: type)
			if (!perms.get(ptype).contains(t))
				return false;
		return true;
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
