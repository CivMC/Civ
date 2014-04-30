package com.untamedears.citadel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.bukkit.entity.Player;

import com.untamedears.citadel.dao.CitadelDao;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.FactionDelete;
import com.untamedears.citadel.entity.FactionMember;
import com.untamedears.citadel.entity.Moderator;
import com.untamedears.citadel.entity.PersonalGroup;
import com.untamedears.citadel.events.GroupChangeEvent;
import com.untamedears.citadel.events.GroupChangeType;

/**
 * User: JonnyD
 * Date: 07/18/12
 * Time: 11:57 PM
 */
public class GroupStorage {
    
    private CitadelDao dao;
    private Map<String, Faction> groupStorage = new TreeMap<String, Faction>();
    private Map<String, Set<String>> memberStorage = new TreeMap<String, Set<String>>();
    private Map<String, Set<String>> moderatorStorage = new TreeMap<String, Set<String>>();
    private Map<String, String> deletedGroups = new TreeMap<String, String>();

    public GroupStorage(CitadelDao dao){
        this.dao = dao;
    }

    public CitadelDao getStorage() {
        return this.dao;
    }

    public static String normalizeName(String name) {
        return name.toLowerCase();
    }

    public boolean isGroup(String groupName) {
        return findGroupByName(groupName) != null;
    }

    public Faction addGroup(Faction group, Player initiator){
        Faction existingGroup = findGroupByName(group.getName());
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

    public Faction findGroupByName(String groupName){
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

    private Set<String> loadMembers(String groupName) {
        if (!isGroup(groupName)) {
            return null;
        }
        String normalizedGroupName = normalizeName(groupName);
        Set<String> members = this.memberStorage.get(normalizedGroupName);
        if (members == null) {
            Set<FactionMember> dbMembers = this.dao.findMembersOfGroup(groupName);
            if (dbMembers != null) {
                members = new TreeSet<String>();
                for (FactionMember fm : dbMembers) {
                    members.add(normalizeName(fm.getMemberName()));
                }
                this.memberStorage.put(normalizedGroupName, members);
            }
        }
        return members;
    }

    public boolean isMember(String groupName, String playerName){
        Set<String> members = loadMembers(groupName);
        return members != null && members.contains(normalizeName(playerName));
    }

    public boolean addMemberToGroup(FactionMember factionMember, Player initiator){
        String groupName = factionMember.getFactionName();
        String playerName = factionMember.getMemberName();
        if (isMember(groupName, playerName)) {
            return false;
        }
        GroupChangeEvent event = new GroupChangeEvent(
            GroupChangeType.ADD_MEMBER, initiator, groupName, playerName);
        Citadel.getStaticServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        Set<String> members = this.memberStorage.get(normalizeName(groupName));
        if (members == null) {
            members = new HashSet<String>();
        }
        members.add(normalizeName(playerName));
        this.dao.save(factionMember);
        return true;
    }

    public boolean removeMemberFromGroup(FactionMember factionMember, Player initiator){
        String groupName = factionMember.getFactionName();
        String playerName = factionMember.getMemberName();
        if (!isMember(groupName, playerName)) {
            return false;
        }
        GroupChangeEvent event = new GroupChangeEvent(
            GroupChangeType.RM_MEMBER, initiator, groupName, playerName);
        Citadel.getStaticServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        Set<String> members = this.memberStorage.get(normalizeName(groupName));
        members.remove(normalizeName(playerName));
        this.dao.delete(factionMember);
        return true;
    }

    public Set<FactionMember> getMembersOfGroup(String groupName) {
        Set<String> members = loadMembers(groupName);
        if (members == null) {
            return null;
        }
        Set<FactionMember> results = new TreeSet<FactionMember>();
        for (String mem : members) {
            results.add(new FactionMember(mem, groupName));
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

    public boolean hasGroupMember(String groupName, String memberName){
        return isMember(groupName, memberName);
    }

    private Set<String> loadModerators(String groupName) {
        if (!isGroup(groupName)) {
            return null;
        }
        String normalizedGroupName = normalizeName(groupName);
        Set<String> moderators = this.moderatorStorage.get(normalizedGroupName);
        if (moderators == null) {
            Set<Moderator> dbModerators = this.dao.findModeratorsOfGroup(groupName);
            if (dbModerators != null) {
                moderators = new TreeSet<String>();
                for (Moderator mod : dbModerators) {
                    moderators.add(normalizeName(mod.getMemberName()));
                }
                this.moderatorStorage.put(normalizedGroupName, moderators);
            }
        }
        return moderators;
    }

    public boolean isModerator(String groupName, String playerName){
        Set<String> moderators = loadModerators(groupName);
        return moderators != null && moderators.contains(normalizeName(playerName));
    }

    public boolean hasGroupModerator(String groupName, String memberName) {
        return isModerator(groupName, memberName);
    }

    public boolean addModeratorToGroup(Moderator moderator, Player initiator){
        String groupName = moderator.getFactionName();
        String playerName = moderator.getMemberName();
        if (isModerator(groupName, playerName)) {
            return false;
        }
        GroupChangeEvent event = new GroupChangeEvent(
            GroupChangeType.ADD_MODERATOR, initiator, groupName, playerName);
        Citadel.getStaticServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        Set<String> moderators = this.moderatorStorage.get(normalizeName(groupName));
        if (moderators == null) {
            moderators = new HashSet<String>();
        }
        moderators.add(normalizeName(playerName));
        this.dao.save(moderator);
        return true;
    }

    public boolean removeModeratorToGroup(Moderator moderator, Player initiator){
        String groupName = moderator.getFactionName();
        String playerName = moderator.getMemberName();
        if (!isModerator(groupName, playerName)) {
            return false;
        }
        GroupChangeEvent event = new GroupChangeEvent(
            GroupChangeType.RM_MODERATOR, initiator, groupName, playerName);
        Citadel.getStaticServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        Set<String> moderators = this.moderatorStorage.get(normalizeName(groupName));
        moderators.remove(normalizeName(playerName));
        this.dao.delete(moderator);
        return true;
    }

    public Set<Moderator> getModeratorsOfGroup(String groupName) {
        Set<String> moderators = loadModerators(groupName);
        if (moderators == null) {
            return null;
        }
        Set<Moderator> results = new TreeSet<Moderator>();
        for (String mod : moderators) {
            results.add(new Moderator(mod, groupName));
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

    public Set<Faction> getGroupsByMember(String playerName) {
        Set<FactionMember> groups = this.dao.findGroupsByMember(playerName);
        if (groups == null) {
            return null;
        }
        Set<Faction> results = new TreeSet<Faction>();
        for (FactionMember member : groups) {
            Faction faction = findGroupByName(member.getFactionName());
            if (faction == null) {
                continue;
            }
            results.add(faction);
        }
        return results;
    }

    public Set<Faction> getGroupsByModerator(String playerName) {
        Set<Moderator> groups = this.dao.findGroupsByModerator(playerName);
        if (groups == null) {
            return null;
        }
        Set<Faction> results = new TreeSet<Faction>();
        for (Moderator moderator : groups) {
            Faction faction = findGroupByName(moderator.getFactionName());
            if (faction == null) {
                continue;
            }
            results.add(faction);
        }
        return results;
    }

    public Set<Faction> getGroupsByFounder(String playerName) {
        Set<Faction> groups = this.dao.findGroupsByFounder(playerName);
        if (groups == null) {
            return null;
        }
        Set<Faction> results = new TreeSet<Faction>();
        for (Faction founder : groups) {
            Faction faction = findGroupByName(founder.getName());
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

    public int getPlayerGroupsAmount(String playerName) {
        return this.dao.countPlayerGroups(playerName);
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
