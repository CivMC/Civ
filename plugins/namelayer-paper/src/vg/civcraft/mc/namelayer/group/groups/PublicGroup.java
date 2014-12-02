package vg.civcraft.mc.namelayer.group.groups;

import java.util.UUID;

import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.group.GroupType;

public class PublicGroup extends Group{

	public PublicGroup(String name, UUID owner, boolean disiplined) {
		super(name, owner, disiplined, null, GroupType.PUBLIC);
	}
}
