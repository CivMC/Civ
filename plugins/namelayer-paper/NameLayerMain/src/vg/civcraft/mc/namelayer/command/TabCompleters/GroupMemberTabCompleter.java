package vg.civcraft.mc.namelayer.command.TabCompleters;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class GroupMemberTabCompleter {
	public static List<String> complete(String groupName, String playerName) {
		Group g = GroupManager.getGroup(groupName);
		if (g != null) {
			ArrayList<String> result = new ArrayList<String>();
			List<UUID> uuids = g.getMembersByName(playerName);
			for (UUID uuid : uuids) {
				result.add(NameAPI.getCurrentName(uuid));
			}
			return result;
		}
		return null;
	}
}
