package vg.civcraft.mc.namelayer.group.groups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.group.GroupType;

/**
 * This class handles default groups.
 * This will include hierarchies for super and lower groups
 */
public class PrivateGroup extends Group{

	private static Map<Group, List<Group>> subGroups = new HashMap<Group, List<Group>>();
	private static Map<Group, Group> superGroup = new HashMap<Group, Group>();
	
	public PrivateGroup(String name, UUID owner, boolean disiplined,
			String password) {
		super(name, owner, disiplined, password, GroupType.PRIVATE);
		subGroups.put(this, db.getSubGroups(name));
		superGroup.put(this, db.getSuperGroup(name));
	}
	@Override
	public void addMember(UUID uuid, PlayerType type){
		db.addMember(uuid, groupName, type);
		players.put(uuid, type);
		for (Group g: subGroups.get(this))
			g.addMember(uuid, PlayerType.SUBGROUP);
	}
	/**
	 * @return Returns the SubGroups in this group.
	 */
	public List<Group> getSubGroups(){
		return subGroups.get(this);
	}
	/**
	 * Checks if a Group is a sub group of this group.
	 * @param subGroup- The Group that may be a sub group.
	 * @return Returns if it is a sub group or not.
	 */
	public boolean isSubGroup(Group subGroup){
		return subGroups.get(this).contains(subGroup);
	}
	/**
	 * @return Returns the SubGroup for this group if there is one.
	 */
	public Group getSuperGroup(){
		return superGroup.get(this);
	}
	/**
	 * @return Returns if this group has a super group or not.
	 */
	public boolean hasSuperGroup(){
		return superGroup.get(this) != null;
	}
	/**
	 * Adds a Sub Group to this group.
	 * @param group- The group to be added as a sub group.
	 * @return Returns true if it was added but false if the group in question is not a PrivateGroup or was already added.
	 */
	public boolean addSubGroup(Group group){
		if (!(group instanceof PrivateGroup) || subGroups.get(this).contains(group))
			return false;
		subGroups.get(this).add(group);
		db.addSubGroup(getName(), group.getName());
		return true;
	}
	/**
	 * Removes a sub group from this group.
	 * @param group- The group to remove.
	 * @return Returns true if successful but false if this group didn't contain that subgroup.
	 */
	public boolean removeSubGroup(Group group){
		if (!subGroups.get(this).contains(group))
			return false;
		subGroups.get(this).remove(group);
		db.removeSubGroup(getName(), group.getName());
		return true;
	}
	/**
	 * Checks if a sub group is on a group.
	 * @param group- The SubGroup.
	 * @return Returns true if it has that subgroup.
	 */
	public boolean hasSubGroup(Group group){
		return subGroups.get(this).contains(group);
	}
	/**
	 * Sets a 
	 * @param group
	 * @return
	 */
	public boolean setSuperGroup(Group group){
		superGroup.remove(this);
		superGroup.put(this, group);
		db.addSubGroup(group.getName(), getName());
		return true;
	}
	
	public void removeSuperGroup(Group group){
		superGroup.remove(this);
		superGroup.put(this, null);
	}
}
