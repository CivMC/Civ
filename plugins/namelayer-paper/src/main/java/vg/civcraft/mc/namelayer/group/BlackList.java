package vg.civcraft.mc.namelayer.group;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameLayerPlugin;

public class BlackList {

    public BlackList() {
    }

    public Set<UUID> getBlacklist(Group g) {
        return g.getBlacklist();
    }

    public Set<UUID> getBlacklist(String groupName) {
        Group group = GroupManager.getGroup(groupName);
        return group == null ? new HashSet<UUID>() : group.getBlacklist();
    }

    public boolean isBlacklisted(Group group, UUID uuid) {
        return group.isBlacklisted(uuid);
    }

    public boolean isBlacklisted(String groupName, UUID uuid) {
        Group group = GroupManager.getGroup(groupName);
        return group != null && group.isBlacklisted(uuid);
    }

    public void initEmptyBlackList(String groupName) {
    }

    public void addBlacklistMember(Group group, UUID uuid, boolean writeToDb) {
        if (!group.isBlacklisted(uuid)) {
            group.addBlacklisted(uuid);
            if (writeToDb) {
                NameLayerPlugin.getGroupManagerDao().addBlackListMember(group.getName(), uuid);
            }
        }
    }

    public void addBlacklistMember(String groupName, UUID uuid, boolean writeToDb) {
        Group group = GroupManager.getGroup(groupName);
        if (group != null) {
            addBlacklistMember(group, uuid, writeToDb);
        }
    }

    public void removeBlacklistMember(Group group, UUID uuid, boolean writeToDb) {
        if (group.isBlacklisted(uuid)) {
            group.removeBlacklisted(uuid);
            if (writeToDb) {
                NameLayerPlugin.getGroupManagerDao().removeBlackListMember(group.getName(), uuid);
            }
        }
    }

    public void removeBlacklistMember(String groupName, UUID uuid, boolean writeToDb) {
        Group group = GroupManager.getGroup(groupName);
        if (group != null) {
            removeBlacklistMember(group, uuid, writeToDb);
        }
    }
}
