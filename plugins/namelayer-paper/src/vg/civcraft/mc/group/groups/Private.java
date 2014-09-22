package vg.civcraft.mc.group.groups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import vg.civcraft.mc.GroupManager.PlayerType;
import vg.civcraft.mc.group.Group;
import vg.civcraft.mc.group.GroupType;

/*
 * This class handles default groups.
 * This will include hierarchies for super and lower groups
 */
public class Private extends Group{

	private static Map<Group, List<Group>> subGroups = new HashMap<Group, List<Group>>();
	private static Map<Group, Group> superGroup = new HashMap<Group, Group>();
	
	public Private(String name, UUID owner, boolean disiplined,
			String password) {
		super(name, owner, disiplined, password, GroupType.PRIVATE);
		subGroups.put(this, db.getSubGroups(name));
		superGroup.put(this, db.getSuperGroup(name));
	}
	
	public void addMember(UUID uuid, PlayerType type){
		db.addMember(uuid, groupName, type);
		players.get(type).add(uuid);
		for (Group g: subGroups.get(this))
			g.addMember(uuid, PlayerType.SUBGROUP);
	}
	
	public List<Group> getSubGroups(){
		return subGroups.get(this);
	}
	
	public boolean isSubGroup(Group subGroup){
		return subGroups.get(this).contains(subGroup);
	}
	
	public Group getSuperGroup(){
		return superGroup.get(this);
	}
	
	public boolean hasSuperGroup(){
		return superGroup.get(this) != null;
	}

	public boolean addSubGroup(Group group){
		if (!(group instanceof Private) || subGroups.get(this).contains(group))
			return false;
		subGroups.get(this).add(group);
		db.addSubGroup(getName(), group.getName());
		return true;
	}
	
	public boolean removeSubGroup(Group group){
		if (!subGroups.get(this).contains(group))
			return false;
		subGroups.get(this).remove(group);
		db.removeSubGroup(getName(), group.getName());
		return true;
	}
	
	public boolean hasSubGroup(Group group){
		return subGroups.get(this).contains(group);
	}
	
	public boolean setSuperGroup(Group group){
		if (!(group instanceof Private))
			return false;
		superGroup.remove(this);
		db.addSubGroup(group.getName(), getName());
		return true;
	}
}
