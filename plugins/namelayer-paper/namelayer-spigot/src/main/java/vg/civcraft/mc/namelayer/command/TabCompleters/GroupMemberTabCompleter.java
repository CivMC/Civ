package vg.civcraft.mc.namelayer.command.TabCompleters;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class GroupMemberTabCompleter {
	public static List<String> complete(String groupName, String playerName,
			Player sender) {
		Group g = GroupManager.getGroup(groupName);
		if (g != null) {
			if (NameAPI.getGroupManager().hasAccess(groupName, sender.getUniqueId(), PermissionType.getPermission("MEMBERS"))) {
				ArrayList<String> result = new ArrayList<>();
				List<UUID> uuids = g.getMembersByName(playerName);
				for (UUID uuid : uuids) {
					result.add(NameAPI.getCurrentName(uuid));
				}
				return result;
			}
		}
		return null;
	}
}