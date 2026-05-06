package vg.civcraft.mc.namelayer.cache;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import vg.civcraft.mc.namelayer.database.NameLayerReadDao;
import vg.civcraft.mc.namelayer.group.Group;

public final class NameLayerGroupCache {

    private final Map<Integer, Group> groupsById;
    private final Map<String, Group> groupsByName;
    private final Map<UUID, Set<Integer>> groupIdsByPlayer;
    private volatile long appliedVersion;

    public NameLayerGroupCache() {
        this.groupsById = new ConcurrentHashMap<>();
        this.groupsByName = new ConcurrentHashMap<>();
        this.groupIdsByPlayer = new ConcurrentHashMap<>();
    }

    public static NameLayerGroupCache loadAll(final NameLayerReadDao dao, final Logger logger) {
        final NameLayerReadDao.GroupLoadSnapshot snapshot = dao.loadAllGroupsSnapshot();
        final NameLayerGroupCache cache = new NameLayerGroupCache();
        for (final Group group : snapshot.groups()) {
            cache.putGroup(group);
        }
        cache.setAppliedVersion(snapshot.cacheVersion());
        logger.log(Level.INFO, "Loaded " + cache.groupsByName.size() + " NameLayer groups into local cache");
        return cache;
    }

    public synchronized Group getByName(final String name) {
        if (name == null) {
            return null;
        }
        return groupsByName.get(name.toLowerCase());
    }

    public synchronized Group getById(final int groupId) {
        return groupsById.get(groupId);
    }

    public synchronized boolean containsName(final String name) {
        return getByName(name) != null;
    }

    public synchronized void putGroup(final Group group) {
        if (group == null || group.getName() == null) {
            return;
        }
        removeGroup(group);
        groupsByName.put(group.getName().toLowerCase(), group);
        for (final int groupId : group.getGroupIds()) {
            groupsById.put(groupId, group);
        }
        for (final UUID uuid : group.getAllMembers()) {
            groupIdsByPlayer.computeIfAbsent(uuid, key -> ConcurrentHashMap.newKeySet()).add(group.getGroupId());
        }
    }

    public synchronized void replaceGroupById(final int groupId, final Group group) {
        final Group oldGroup = groupsById.get(groupId);
        if (oldGroup != null) {
            removeGroup(oldGroup);
        } else {
            groupsById.remove(groupId);
        }
        if (group != null) {
            putGroup(group);
        }
    }

    public synchronized void removeGroup(final Group group) {
        if (group == null || group.getName() == null) {
            return;
        }
        final Group oldGroup = groupsByName.remove(group.getName().toLowerCase());
        final Group groupToRemove = oldGroup == null ? group : oldGroup;
        for (final int groupId : groupToRemove.getGroupIds()) {
            groupsById.remove(groupId);
        }
        for (final UUID uuid : groupToRemove.getAllMembers()) {
            final Set<Integer> groupIds = groupIdsByPlayer.get(uuid);
            if (groupIds == null) {
                continue;
            }
            groupIds.remove(groupToRemove.getGroupId());
            if (groupIds.isEmpty()) {
                groupIdsByPlayer.remove(uuid);
            }
        }
    }

    public synchronized Group removeGroupById(final int groupId) {
        final Group group = groupsById.get(groupId);
        if (group != null) {
            removeGroup(group);
        } else {
            groupsById.remove(groupId);
        }
        return group;
    }

    public synchronized List<String> getGroupNames(final UUID uuid) {
        final Set<Integer> groupIds = groupIdsByPlayer.get(uuid);
        if (groupIds == null || groupIds.isEmpty()) {
            return new ArrayList<>();
        }
        final Set<String> names = new LinkedHashSet<>();
        for (final int groupId : groupIds) {
            final Group group = groupsById.get(groupId);
            if (group != null) {
                names.add(group.getName());
            }
        }
        return new ArrayList<>(names);
    }

    public synchronized long getAppliedVersion() {
        return appliedVersion;
    }

    public synchronized void setAppliedVersion(final long appliedVersion) {
        this.appliedVersion = Math.max(0L, appliedVersion);
    }
}
