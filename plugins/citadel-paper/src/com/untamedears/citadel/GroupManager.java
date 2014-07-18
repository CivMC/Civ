package com.untamedears.citadel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.untamedears.citadel.dao.CitadelDao;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.FactionDelete;
import com.untamedears.citadel.entity.FactionMember;
import com.untamedears.citadel.entity.Moderator;
import com.untamedears.citadel.entity.PersonalGroup;
import com.untamedears.citadel.events.GroupChangeEvent;
import com.untamedears.citadel.events.GroupChangeType;

public class GroupManager {
    private CitadelDao dao;
    private Map<String, Faction> groupStorage = new TreeMap<String, Faction>();
    private Map<String, Set<UUID>> memberStorage = new TreeMap<String, Set<UUID>>();
    private Map<String, Set<UUID>> moderatorStorage = new TreeMap<String, Set<UUID>>();
    private Map<String, String> deletedGroups = new TreeMap<String, String>();

    public GroupManager() {}

    public void initialize(CitadelDao dao) {
        this.dao = dao;
        batchRemoveDeletedGroups();
        // If the batch update times out, this will load the remaining deleted groups
        loadDeletedGroups();
    }

    public CitadelDao getStorage() {
        return this.dao;
    }

    public static String normalizeName(String name) {
        return name.toLowerCase();
    }

    public boolean isGroup(String groupName) {
        return getGroup(groupName) != null;
    }

    public Faction addGroup(Faction group, Player initiator){
        Faction existingGroup = getGroup(group.getName());
        if (existingGroup != null) {
            // This is also used to save DB changes to the Faction
            existingGroup.Copy(group);
            this.dao.save(group);
            return existingGroup;
        }
        GroupChangeEvent event = new GroupChangeEvent(
            GroupChangeType.CREATE, initiator, group.getName(), null);
        Citadel.getStaticServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return null;
        }
        String normalizedName = group.getNormalizedName();
        this.groupStorage.put(normalizedName, group);
        this.memberStorage.remove(normalizedName);
        this.moderatorStorage.remove(normalizedName);
        this.dao.save(group);
        return group;
    }

    public void removeGroup(Faction group, Player initiator) {
        removeGroup(group, null, initiator);
    }

    public void removeGroup(Faction group, PersonalGroup redirectToGroup, Player initiator){
        final String groupName = group.getName();
        if (!isGroup(groupName) || isDeleted(groupName)) {
            return;
        }
        GroupChangeEvent event = new GroupChangeEvent(
            GroupChangeType.DELETE, initiator, group.getName(), null);
        Citadel.getStaticServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        final String normalizedName = group.getNormalizedName();
        removeAllModeratorsFromGroup(normalizedName);
        removeAllMembersFromGroup(normalizedName);
        if (redirectToGroup != null) {
            FactionDelete facDel = new FactionDelete();
            facDel.setDeletedFaction(group.getName());
            facDel.setPersonalGroup(redirectToGroup.getGroupName());
            this.dao.save(facDel);

            deletedGroups.put(normalizedName, normalizeName(redirectToGroup.getGroupName()));
            group.setDeleted(true);
            this.dao.save(group);
        } else {
            this.groupStorage.remove(normalizedName);
            this.dao.delete(group);
        }
    }

    public Faction getGroup(String groupName){
        Faction group = this.groupStorage.get(normalizeName(groupName));
        if (group == null) {
            group = this.dao.findGroupByName(groupName);
            if (group == null) {
                return null;
            }
            this.groupStorage.put(group.getNormalizedName(), group);
            this.memberStorage.remove(groupName);
            this.moderatorStorage.remove(groupName);
        }
        return group;
    }

    private Set<UUID> loadMembers(String groupName) {
        if (!isGroup(groupName)) {
            return null;
        }
        String normalizedGroupName = normalizeName(groupName);
        Set<UUID> members = this.memberStorage.get(normalizedGroupName);
        if (members == null) {
            Set<FactionMember> dbMembers = this.dao.findMembersOfGroup(groupName);
            if (dbMembers != null) {
                members = new TreeSet<UUID>();
                for (FactionMember fm : dbMembers) {
                    try {
                        members.add(UUID.fromString(fm.getMemberName()));
                    } catch (Exception ex) {}
                }
                this.memberStorage.put(normalizedGroupName, members);
            }
        }
        return members;
    }

    public boolean isMember(String groupName, UUID accountId) {
        final Set<UUID> members = loadMembers(groupName);
        return members != null && members.contains(accountId);
    }

    public boolean addMemberToGroup(String groupName, UUID memberAccountId, Player initiator){
        if (isMember(groupName, memberAccountId)) {
            return false;
        }
        GroupChangeEvent event = new GroupChangeEvent(
            GroupChangeType.ADD_MEMBER, initiator, groupName, memberAccountId);
        Citadel.getStaticServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        final String normGroupName = normalizeName(groupName);
        Set<UUID> members = this.memberStorage.get(normGroupName);
        if (members == null) {
            members = new HashSet<UUID>();
            this.memberStorage.put(normGroupName, members);
        }
        members.add(memberAccountId);
        FactionMember factionMember = new FactionMember(memberAccountId.toString(), groupName);
        this.dao.save(factionMember);
        return true;
    }

    public boolean removeMemberFromGroup(String groupName, UUID memberAccountId, Player initiator){
        if (!isMember(groupName, memberAccountId)) {
            return false;
        }
        GroupChangeEvent event = new GroupChangeEvent(
            GroupChangeType.RM_MEMBER, initiator, groupName, memberAccountId);
        Citadel.getStaticServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        Set<UUID> members = this.memberStorage.get(normalizeName(groupName));
        members.remove(memberAccountId);
        FactionMember factionMember = new FactionMember(memberAccountId.toString(), groupName);
        this.dao.delete(factionMember);
        return true;
    }

    public Set<FactionMember> getMembersOfGroup(String groupName) {
        Set<UUID> members = loadMembers(groupName);
        if (members == null) {
            return null;
        }
        Set<FactionMember> results = new TreeSet<FactionMember>();
        for (UUID accountId : members) {
            results.add(new FactionMember(accountId.toString(), groupName));
        }
        return results;
    }

    public boolean removeAllMembersFromGroup(String groupName){
        if (!isGroup(groupName)) {
            return false;
        }
        this.memberStorage.remove(normalizeName(groupName));
        this.dao.removeAllMembersFromGroup(groupName);
        return true;
    }

    public boolean hasGroupMember(String groupName, UUID accountId){
        return isMember(groupName, accountId);
    }

    private Set<UUID> loadModerators(String groupName) {
        if (!isGroup(groupName)) {
            return null;
        }
        String normalizedGroupName = normalizeName(groupName);
        Set<UUID> moderators = this.moderatorStorage.get(normalizedGroupName);
        if (moderators == null) {
            Set<Moderator> dbModerators = this.dao.findModeratorsOfGroup(groupName);
            if (dbModerators != null) {
                moderators = new TreeSet<UUID>();
                for (Moderator mod : dbModerators) {
                    try {
                        moderators.add(UUID.fromString(mod.getMemberName()));
                    } catch (Exception ex) {}
                }
                this.moderatorStorage.put(normalizedGroupName, moderators);
            }
        }
        return moderators;
    }

    public boolean isModerator(String groupName, UUID accountId){
        Set<UUID> moderators = loadModerators(groupName);
        return moderators != null && moderators.contains(accountId);
    }

    public boolean hasGroupModerator(String groupName, UUID accountId) {
        return isModerator(groupName, accountId);
    }

    public boolean addModeratorToGroup(String groupName, UUID accountId, Player initiator){
        if (isModerator(groupName, accountId)) {
            return false;
        }
        GroupChangeEvent event = new GroupChangeEvent(
            GroupChangeType.ADD_MODERATOR, initiator, groupName, accountId);
        Citadel.getStaticServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        final String normGroupName = normalizeName(groupName);
        Set<UUID> moderators = this.moderatorStorage.get(normGroupName);
        if (moderators == null) {
            moderators = new HashSet<UUID>();
            this.moderatorStorage.put(normGroupName, moderators);
        }
        moderators.add(accountId);
        Moderator moderator = new Moderator(accountId.toString(), groupName);
        this.dao.save(moderator);
        return true;
    }

    public boolean removeModeratorFromGroup(String groupName, UUID accountId, Player initiator){
        if (!isModerator(groupName, accountId)) {
            return false;
        }
        GroupChangeEvent event = new GroupChangeEvent(
            GroupChangeType.RM_MODERATOR, initiator, groupName, accountId);
        Citadel.getStaticServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        final String normGroupName = normalizeName(groupName);
        Set<UUID> moderators = this.moderatorStorage.get(normGroupName);
        moderators.remove(accountId);
        Moderator moderator = new Moderator(accountId.toString(), groupName);
        this.dao.delete(moderator);
        return true;
    }

    public Set<Moderator> getModeratorsOfGroup(String groupName) {
        Set<UUID> moderators = loadModerators(groupName);
        if (moderators == null) {
            return null;
        }
        Set<Moderator> results = new TreeSet<Moderator>();
        for (UUID accountId : moderators) {
            results.add(new Moderator(accountId.toString(), groupName));
        }
        return results;
    }

    public boolean removeAllModeratorsFromGroup(String groupName) {
        if (!isGroup(groupName)) {
            return false;
        }
        this.moderatorStorage.remove(normalizeName(groupName));
        this.dao.removeAllModeratorsFromGroup(groupName);
        return true;
    }

    public Set<Faction> getGroupsByMember(UUID accountId) {
        Set<FactionMember> groups = this.dao.findGroupsByMember(accountId.toString());
        if (groups == null) {
            return null;
        }
        Set<Faction> results = new TreeSet<Faction>();
        for (FactionMember member : groups) {
            Faction faction = getGroup(member.getFactionName());
            if (faction == null) {
                continue;
            }
            results.add(faction);
        }
        return results;
    }

    public Set<Faction> getGroupsByModerator(UUID accountId) {
        Set<Moderator> groups = this.dao.findGroupsByModerator(accountId.toString());
        if (groups == null) {
            return null;
        }
        Set<Faction> results = new TreeSet<Faction>();
        for (Moderator moderator : groups) {
            Faction faction = getGroup(moderator.getFactionName());
            if (faction == null) {
                continue;
            }
            results.add(faction);
        }
        return results;
    }

    public Set<Faction> getGroupsByFounder(UUID accountId) {
        Set<Faction> groups = this.dao.findGroupsByFounder(
            accountId.toString());
        if (groups == null) {
            return null;
        }
        Set<Faction> results = new TreeSet<Faction>();
        for (Faction founder : groups) {
            Faction faction = getGroup(founder.getName());
            if (faction == null) {
                continue;
            }
            results.add(faction);
        }
        return results;
    }

    public int getGroupsAmount() {
        return this.dao.countGroups();
    }

    public int getPlayerGroupsAmount(UUID accountId) {
        return this.dao.countPlayerGroups(accountId);
    }

    public boolean isDeleted(String groupName) {
        final String normalizedName = normalizeName(groupName);
        return deletedGroups.containsKey(normalizedName);
    }

    public String mapDeletedGroup(String groupName) {
        final String normalizedName = normalizeName(groupName);
        if (!deletedGroups.containsKey(normalizedName)) {
            return normalizedName;
        }
        return deletedGroups.get(normalizedName);
    }

    public String getDelegatedGroupName(String groupName) {
        final String delegatedName = mapDeletedGroup(groupName);
        if (delegatedName != null) {
            return delegatedName;
        }
        return groupName;
    }

    public Faction getDelegatedGroup(String groupName) {
        return getGroup(getDelegatedGroupName(groupName));
    }

    public void loadDeletedGroups() {
        for (FactionDelete facDel : this.dao.loadFactionDeletions()) {
            deletedGroups.put(
                normalizeName(facDel.getDeletedFaction()),
                normalizeName(facDel.getPersonalGroup()));
        }
    }

    public void batchRemoveDeletedGroups() {
    	this.dao.batchRemoveDeletedGroups();
    }
}
