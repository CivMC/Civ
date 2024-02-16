package vg.civcraft.mc.namelayer.permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;
import vg.civcraft.mc.namelayer.group.Group;

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
		//to save ourselves from trouble later, we ensure that every perm type has at least an empty list
		for(PlayerType pType : PlayerType.values()) {
			perms.computeIfAbsent(pType, k -> new ArrayList<>());
		}
	}
	/**
	 * Checks if a certain PlayerType has the given permission. DONT USE THIS DIRECTLY. Use GroupManager.hasAccess() instead!
	 * @param playerType The PlayerType in question.
	 * @param perm The PermissionType to check for.
	 * @return return true if this type of player has this type of perm, false otherwise
	 */
	public boolean hasPermission(PlayerType playerType, PermissionType perm){
		if (playerType == null || perm == null) {
			return false;
		}
		List<PermissionType> per = perms.get(playerType);
		if (per == null || per.isEmpty()){
			return false;
		}
		if (per.contains(perm)){
			return true;
		}
		return false;
	}

	/**
	 * Lists the permissions types for a given PlayerType for the specific GroupPermission.
	 * @param type The PlayerType to check for.
	 * @return Returns a String representation of the permissions. Should be sent to the player in this form.
	 */
	public String listPermsforPlayerType(PlayerType type){
		String x = "The permission types are: ";
		for (PermissionType pType: perms.get(type)) {
			if (pType != null) {
				x += pType.getName() + " ";
			}
		}
		return x;
	}

	/**
	 * Adds a PermissionType to a PlayerType.
	 * @param pType The PlayerType.
	 * @param permType The PermissionType.
	 * @return Returns false if the PlayerType already has the permission.
	 */
	public boolean addPermission(PlayerType pType, PermissionType permType) {
		return addPermission(pType,permType,true);
	}

	public boolean addPermission(PlayerType pType, PermissionType permType, boolean savetodb) {
		List<PermissionType> playerPerms = perms.get(pType);
		if (playerPerms == null || playerPerms.contains(permType)) {
			return false;
		}
		playerPerms.add(permType);
		if (savetodb) {
			db.addPermission(group.getName(), pType.name(), Collections.singletonList(permType));
		}
		return true;
	}

	/**
	 * Removes the PermissionType from a PlayerType.
	 * @param pType The PlayerType to get the PermissionType removed from.
	 * @param permType The PermissionType.
	 * @return Returns false if the PlayerType doesn't have that permission.
	 */
	public boolean removePermission(PlayerType pType, PermissionType permType){
		return removePermission(pType,permType,true);
	}
	
	public boolean removePermission(PlayerType pType, PermissionType permType, boolean savetodb) {
		List<PermissionType> playerPerms = perms.get(pType);
		if (playerPerms == null || !playerPerms.contains(permType)) {
			return false;
		}
		playerPerms.remove(permType);
		if (savetodb) {
			db.removePermissionAsync(group.getName(), pType, permType);
		}
		return true;
	}

	/**
	 * Returns the first PlayerType with a specific permission.
	 * @param type The PermissionType you are looking for.
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
