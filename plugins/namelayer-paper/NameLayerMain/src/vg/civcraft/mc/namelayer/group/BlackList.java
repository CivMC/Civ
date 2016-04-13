package vg.civcraft.mc.namelayer.group;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import vg.civcraft.mc.namelayer.NameLayerPlugin;

public class BlackList {
	private Map<String, Set<UUID>> blackListsByGroupName;
	
	public BlackList() {
		blackListsByGroupName = new HashMap<String, Set<UUID>>();
	}
	
	public Set<UUID> getBlacklist(Group g) {
		return getBlacklist(g.getName());
	}
	
	public Set<UUID> getBlacklist(String groupName) {
		Set<UUID> black = blackListsByGroupName.get(groupName);		
		if (black == null) {
			loadBlacklistMembersFromDb(groupName);
			black = blackListsByGroupName.get(groupName);		
		}
		return black;
	}
	
	public boolean isBlacklisted(Group group, UUID uuid) {
		return isBlacklisted(group.getName(), uuid);
	}
	
	public boolean isBlacklisted(String groupName, UUID uuid) {
		Set <UUID> ids = blackListsByGroupName.get(groupName);
		if (ids == null) {
			loadBlacklistMembersFromDb(groupName);
			ids = blackListsByGroupName.get(groupName);
		}
		if (ids != null && ids.contains(uuid)) {
			return true;
		}
		return false;
	}
	
	public void loadBlacklistMembersFromDb(String groupName) {
		blackListsByGroupName.put(groupName, NameLayerPlugin.getGroupManagerDao().getBlackListMembers(groupName));
	}
	
	public void initEmptyBlackList(String groupName) {
		blackListsByGroupName.put(groupName, new HashSet<UUID>());
	}
	
	public void addBlacklistMember(Group group, UUID uuid, boolean writeToDb) {
		addBlacklistMember(group.getName(), uuid, writeToDb);
	}
	
	public void addBlacklistMember(String groupName, UUID uuid, boolean writeToDb) {
		Set <UUID> ids = blackListsByGroupName.get(groupName);
		if (ids == null) {
			loadBlacklistMembersFromDb(groupName);
			ids = blackListsByGroupName.get(groupName);
		}
		if (ids != null && !ids.contains(uuid)) {
			ids.add(uuid);
			if (writeToDb) {
				NameLayerPlugin.getGroupManagerDao().addBlackListMember(groupName, uuid);
			}
		}
	}
	
	public void removeBlacklistMember(Group group, UUID uuid, boolean writeToDb) {
		removeBlacklistMember(group.getName(), uuid, writeToDb);
	}
	
	public void removeBlacklistMember(String groupName, UUID uuid, boolean writeToDb) {
		Set <UUID> ids = blackListsByGroupName.get(groupName);
		if (ids == null) {
			loadBlacklistMembersFromDb(groupName);
			ids = blackListsByGroupName.get(groupName);
		}
		if (ids != null && ids.contains(uuid)) {
			ids.remove(uuid);
			if (writeToDb) {
				NameLayerPlugin.getGroupManagerDao().removeBlackListMember(groupName, uuid);
			}
		}
	}
	
	public void removeFromCache(String groupName) {
		blackListsByGroupName.remove(groupName);
	}
	
}
