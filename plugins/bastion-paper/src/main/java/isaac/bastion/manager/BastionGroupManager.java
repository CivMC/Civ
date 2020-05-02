/**
 * Created by Aleksey on 14.07.2017.
 */

package isaac.bastion.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import isaac.bastion.BastionBlock;
import isaac.bastion.BastionGroup;
import isaac.bastion.Permissions;
import isaac.bastion.storage.BastionGroupStorage;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class BastionGroupManager {
	private static class FoundGroups {
		public Group group;
		public Group allowedGroup;
	}

	private BastionGroupStorage storage;

	public BastionGroupManager(BastionGroupStorage storage) {
		this.storage = storage;
	}

	public void addAllowedGroup(Player player, String groupName, String allowedGroupName) {
		FoundGroups groups = findGroups(player, groupName, allowedGroupName);

		if(groups == null) return;

		if(this.storage.addAllowedGroup(groups.group, groups.allowedGroup)) {
			player.sendMessage(ChatColor.GREEN + "Group [" + allowedGroupName + "] added to bastion group [" + groupName + "]");
		} else {
			player.sendMessage(ChatColor.YELLOW + "Group [" + allowedGroupName + "] is already in bastion group [" + groupName + "]");
		}
	}

	public void deleteAllowedGroup(Player player, String groupName, String allowedGroupName) {
		FoundGroups groups = findGroups(player, groupName, allowedGroupName);

		if(groups == null) return;

		if(this.storage.deleteAllowedGroup(groups.group, groups.allowedGroup)) {
			player.sendMessage(ChatColor.GREEN + "Group [" + allowedGroupName + "] deleted from bastion group [" + groupName + "]");
		} else {
			player.sendMessage(ChatColor.YELLOW + "Group [" + allowedGroupName + "] is not in bastion group [" + groupName + "]");
		}
	}

	public void listAllowedGroups(Player player, String groupName) {
		Group group = findGroup(player, groupName);

		if(group == null) return;

		List<BastionGroup> bastionGroups = this.storage.getBastionGroups(group);

		if(bastionGroups.isEmpty()) {
			player.sendMessage(ChatColor.YELLOW + "There are no groups in bastion group [" + groupName + "]");
			return;
		}

		Map<String, String> map = new HashMap<>();

		for(BastionGroup bastionGroup : bastionGroups) {
			for(int allowedGroupId : bastionGroup.getAllowedGroupIds()) {
				Group allowedGroup = GroupManager.getGroup(allowedGroupId);

				if(allowedGroup != null && allowedGroup.isValid()) {
					map.put(allowedGroup.getName().toLowerCase(), allowedGroup.getName());
				}
			}
		}

		if(map.size() == 0) {
			player.sendMessage(ChatColor.YELLOW + "There are no groups in bastion group [" + groupName + "]");
			return;
		}

		List<String> result = new ArrayList<>();
		result.addAll(map.values());

		Collections.sort(result);

		player.sendMessage(ChatColor.GREEN + "Bastion group [" + groupName + "] has following allowed groups:");

		for(String allowedGroupName : result) {
			player.sendMessage(ChatColor.GREEN + "- " + allowedGroupName);
		}
	}

	public boolean canPlaceBlock(Player player, Set<BastionBlock> bastionBlocks) {
		for(BastionBlock bastionBlock : bastionBlocks) {
			Reinforcement rein = bastionBlock.getReinforcement();
			if (rein == null) {
				return false;
			}
			if(!this.storage.isAllowedGroup(rein.getGroup(), rein.getGroup())) {
				return false;
			}
		}

		return true;
	}

	public Group findFirstAllowedGroup(Player player, BastionBlock bastionBlock) {
		Reinforcement rein = bastionBlock.getReinforcement();

		List<BastionGroup> bastionGroups = this.storage.getBastionGroups(rein.getGroup());

		for(BastionGroup bastionGroup : bastionGroups) {
			for(int allowedGroupId : bastionGroup.getAllowedGroupIds()) {
				Group allowedGroup = GroupManager.getGroup(allowedGroupId);

				if(allowedGroup != null && allowedGroup.isValid()&& allowedGroup.isMember(player.getUniqueId())) {
					return allowedGroup;
				}
			}
		}

		return null;
	}

	public boolean isAllowedGroup(Group group, Group allowedGroup) {
		return this.storage.isAllowedGroup(group, allowedGroup);
	}

	private Group findGroup(Player player, String groupName) {
		Group group = GroupManager.getGroup(groupName);

		if(group == null || !group.isValid()) {
			player.sendMessage(ChatColor.RED + "Group [" + groupName + "] doesn't exist");
			return null;
		}

		PermissionType perm = PermissionType.getPermission(Permissions.BASTION_MANAGE_GROUPS);

		if(!NameAPI.getGroupManager().hasAccess(group, player.getUniqueId(), perm)) {
			player.sendMessage(ChatColor.RED + "You haven't permissions to manage bastion group [" + groupName + "]");
			return null;
		}

		return group;
	}

	private FoundGroups findGroups(Player player, String groupName, String allowedGroupName) {
		Group group = findGroup(player, groupName);

		if(group == null) return null;

		Group allowedGroup = GroupManager.getGroup(allowedGroupName);

		if(allowedGroup == null || !allowedGroup.isValid()) {
			player.sendMessage(ChatColor.RED + "Group [" + allowedGroupName + "] doesn't exist");
			return null;
		}

		FoundGroups result = new FoundGroups();
		result.group = group;
		result.allowedGroup = allowedGroup;

		return result;
	}
}
