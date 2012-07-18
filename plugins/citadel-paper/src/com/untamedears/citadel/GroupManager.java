package com.untamedears.citadel;

import java.util.Set;

import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.FactionMember;
import com.untamedears.citadel.entity.Moderator;

/**
 * User: JonnyD
 * Date: 07/18/12
 * Time: 11:57 PM
 */
public class GroupManager {
	private GroupStorage storage;

	public GroupManager(){}

	public GroupStorage getStorage() {
		return this.storage;
	}
	
	public void setStorage(GroupStorage storage){
		this.storage = storage;
	}
	
	public boolean isGroup(String groupName){
		return this.storage.isGroup(groupName);
	}
	
	public FactionMember getMemberFromGroup(String groupName, String memberName){
		return this.storage.findMemberByGroup(groupName, memberName);
	}
	
	public Faction getGroup(String groupName){
		return this.storage.findGroupByName(groupName);
	}
	
	public void addGroup(Faction group){
		this.storage.addGroup(group);
	}
	
	public void removeGroup(Faction group){
		this.storage.removeGroup(group);
	}

	public Set<FactionMember> getMembersOfGroup(String groupName) {
		return this.storage.findMembersOfGroup(groupName);
	}
	
	public boolean hasGroupMember(String groupName, String memberName){
		return this.storage.hasGroupMember(groupName, memberName);
	}
	
	public void addMemberToGroup(String groupName, String memberName){
		addMemberToGroup(new FactionMember(memberName, groupName));
	}
	
	public void addMemberToGroup(FactionMember factionMember){
		this.storage.addMemberToGroup(factionMember);
	}
	
	public void removeMemberFromGroup(String groupName, String memberName){
		removeMemberFromGroup(new FactionMember(memberName, groupName));
	}
	
	public void removeMemberFromGroup(FactionMember factionMember){
		this.storage.removeMemberFromGroup(factionMember);
	}
	
	public void removeAllMembersFromGroup(String groupName){
		this.storage.removeAllMembersFromGroup(groupName);
	}
	
	public Set<Faction> getGroupsByMember(String memberName){
		return this.storage.findGroupsByMember(memberName);
	}
	
	public Set<Faction> getGroupsByFounder(String memberName){
		return this.storage.findGroupsByFounder(memberName);
	}
	
	public boolean hasGroupModerator(String groupName, String memberName){
		return this.storage.hasGroupModerator(groupName, memberName);
	}
	
	public void addModeratorToGroup(String groupName, String memberName){
		addModeratorToGroup(new Moderator(memberName, groupName));
	}
	
	public void addModeratorToGroup(Moderator moderator){
		this.storage.addModeratorToGroup(moderator);
	}
	
	public void removeModeratorFromGroup(String groupName, String memberName){
		removeModeratorFromGroup(new Moderator(memberName, groupName));
	}
	
	public void removeModeratorFromGroup(Moderator moderator){
		this.storage.removeModeratorToGroup(moderator);
	}

	public Set<Faction> getGroupsByModerator(String memberName) {
		return this.storage.findGroupsByModerator(memberName);
	}

	public Set<Moderator> getModeratorsOfGroup(String groupName) {
		return this.storage.findModeratorsOfGroup(groupName);
	}

	public void removeAllModeratorsFromGroup(String groupName) {
		this.storage.removeAllModeratorsFromGroup(groupName);
	}

	public int getGroupsAmount() {
		return this.storage.findGroupsAmount();
	}
	
	public int getPlayerGroupsAmount(String playerName){
		return this.storage.findPlayerGroupsAmount(playerName);
	}
}
