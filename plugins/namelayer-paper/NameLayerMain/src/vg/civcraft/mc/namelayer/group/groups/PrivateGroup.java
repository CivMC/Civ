package vg.civcraft.mc.namelayer.group.groups;

import java.util.UUID;

import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.group.GroupType;

public class PrivateGroup extends Group {

	public PrivateGroup(String name, UUID owner, boolean disiplined, String password, int id) {
		super(name, owner, disiplined, password, GroupType.PRIVATE, id);
	}
}
