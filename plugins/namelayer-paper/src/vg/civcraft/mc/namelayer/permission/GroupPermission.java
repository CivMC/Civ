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
	/**
	 * Checks if a certain PlayerType has one of the permissions wanted.
	 * @param ptype- The PlayerType in question.
	 * @param type- The PermissionType to check for.
	 * @return
	 */
	public boolean isAccessible(PlayerType ptype, PermissionType... type){
		if (type == null || type.length == 0)
			return true; // If the player is even a member return true
		if (group instanceof PublicGroup){
			for (int x = 0; x < type.length; x++)
			switch (type[x]){
			case DOORS:
			case CHESTS:
				return true;
			default:;
			}
		}
		boolean hasPerm = false;
		for (PermissionType t: type){
			List<PermissionType> p = perms.get(ptype);
			if (p == null){
				continue;
			} else if (p.isEmpty()){
				continue;
			} else if (p.contains(t)){
				hasPerm = true;
			}
		}
		return hasPerm;
	}
	/**
	 * This is like isAccessible but player must have all the PermissionTypes for this method to return true.
	 * @param ptype- The PlayerType in question.
	 * @param type- The PermissionType to check for.
	 * @return
	 */
	public boolean hasAllPermissions(PlayerType ptype, PermissionType... type){
		for (PermissionType t: type)
			if (!perms.get(ptype).contains(t))
				return false;
		return true;
	}
	/**
	 * Lists the permissions types for a given PlayerType for the specific GroupPermission.
	 * @param type- The PlayerType to check for.
	 * @return Returns a String representation of the permissions. Should be sent to the player in this form.
	 */
	public String listPermsforPlayerType(PlayerType type){
		String x = "The permission types are: ";
		for (PermissionType pType: perms.get(type))
			x += pType.name() + " ";
		return x;
	}
	/**
	 * Adds a PermissionType to a PlayerType.
	 * @param pType- The PlayerType.
	 * @param permType- The PermissionType.
	 * @return Returns false if the PlayerType already has the permission.
	 */
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
	/**
	 * Removes the PermissionType from a PlayerType.
	 * @param pType- The PlayerType to get the PermissionType removed from.
	 * @param permType- The PermissionType.
	 * @return Returns false if the PlayerType doesn't have that permission.
	 */
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
	/**
	 * Returns the first PlayerType with a specific permission.
	 * @param type- The PermissionType you are looking for.
	 * @return Returns the first PlayerType with the permission or false if none was found.
	 */
	public PlayerType getFirstWithPerm(PermissionType type){
		for (PlayerType pType: perms.keySet()){
			if (perms.get(pType).contains(type))
				return pType;
		}
		return null;
	}
}
