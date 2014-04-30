package com.untamedears.citadel;

import java.util.Set;

import org.bukkit.entity.Player;

import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.FactionMember;
import com.untamedears.citadel.entity.Moderator;
import com.untamedears.citadel.entity.PersonalGroup;

/**
 * User: JonnyD
 * Date: 07/18/12
 * Time: 11:57 PM
 */
public class GroupManager {
	private GroupStorage storage;

	public GroupManager(){}

    public void initialize(GroupStorage storage) {
        setStorage(storage);
        storage.batchRemoveDeletedGroups();
        
        // If the batch update times out, this will load the remaining deleted groups
        storage.loadDeletedGroups();
    }

	public GroupStorage getStorage() {
		return this.storage;
	}
	
	public void setStorage(GroupStorage storage){
		this.storage = storage;
	}
	
	public boolean isGroup(String groupName){
		return this.storage.isGroup(groupName);
	}
	
	public Faction getGroup(String groupName){
		return this.storage.findGroupByName(groupName);
	}
	
	public void addGroup(Faction group, Player initiator){
		this.storage.addGroup(group, initiator);
	}

	public void removeGroup(Faction group, Player initiator){
		this.storage.removeGroup(group, null, initiator);
	}

    public void removeGroup(Faction group, PersonalGroup redirectToGroup, Player initiator){
		this.storage.removeGroup(group, redirectToGroup, initiator);
    }

	public Set<FactionMember> getMembersOfGroup(String groupName) {
		return this.storage.getMembersOfGroup(groupName);
	}
	
	public boolean hasGroupMember(String groupName, String memberName){
		return this.storage.hasGroupMember(groupName, memberName);
	}
	
	public void addMemberToGroup(String groupName, String memberName, Player initiator){
		addMemberToGroup(new FactionMember(memberName, groupName), initiator);
	}
	
	public void addMemberToGroup(FactionMember factionMember, Player initiator){
		this.storage.addMemberToGroup(factionMember, initiator);
	}
	
	public void removeMemberFromGroup(String groupName, String memberName, Player initiator){
		removeMemberFromGroup(new FactionMember(memberName, groupName), initiator);
	}
	
	public void removeMemberFromGroup(FactionMember factionMember, Player initiator){
		this.storage.removeMemberFromGroup(factionMember, initiator);
	}
	
	public void removeAllMembersFromGroup(String groupName){
		this.storage.removeAllMembersFromGroup(groupName);
	}
	
	public Set<Faction> getGroupsByMember(String memberName){
		return this.storage.getGroupsByMember(memberName);
	}
	
	public Set<Faction> getGroupsByFounder(String memberName){
		return this.storage.getGroupsByFounder(memberName);
	}
	
	public boolean hasGroupModerator(String groupName, String memberName){
		return this.storage.hasGroupModerator(groupName, memberName);
	}
	
	public void addModeratorToGroup(String groupName, String memberName, Player initiator){
		addModeratorToGroup(new Moderator(memberName, groupName), initiator);
	}
	
	public void addModeratorToGroup(Moderator moderator, Player initiator){
		this.storage.addModeratorToGroup(moderator, initiator);
	}
	
	public void removeModeratorFromGroup(String groupName, String memberName, Player initiator){
		removeModeratorFromGroup(new Moderator(memberName, groupName), initiator);
	}
	
	public void removeModeratorFromGroup(Moderator moderator, Player initiator){
		this.storage.removeModeratorToGroup(moderator, initiator);
	}

	public Set<Faction> getGroupsByModerator(String memberName) {
		return this.storage.getGroupsByModerator(memberName);
	}

	public Set<Moderator> getModeratorsOfGroup(String groupName) {
		return this.storage.getModeratorsOfGroup(groupName);
	}

	public void removeAllModeratorsFromGroup(String groupName) {
		this.storage.removeAllModeratorsFromGroup(groupName);
	}

	public int getGroupsAmount() {
		return this.storage.getGroupsAmount();
	}
	
	public int getPlayerGroupsAmount(String playerName){
		return this.storage.getPlayerGroupsAmount(playerName);
	}

    public boolean isDeleted(String groupName) {
		return this.storage.isDeleted(groupName);
    }

    public String mapDeletedGroup(String groupName) {
		return this.storage.mapDeletedGroup(groupName);
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
}
