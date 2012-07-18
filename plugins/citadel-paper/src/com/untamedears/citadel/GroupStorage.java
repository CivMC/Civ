package com.untamedears.citadel;

import java.util.HashSet;
import java.util.Set;

import com.untamedears.citadel.dao.CitadelDao;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.FactionMember;
import com.untamedears.citadel.entity.Moderator;

/**
 * User: JonnyD
 * Date: 07/18/12
 * Time: 11:57 PM
 */
public class GroupStorage {
	
	private CitadelDao dao;

	public GroupStorage(CitadelDao dao){
		this.dao = dao;
	}

	public CitadelDao getStorage() {
		return this.dao;
	}
	
	public void addGroup(Faction group){
		this.dao.save(group);
	}
	
	public void removeGroup(Faction group){
		this.dao.delete(group);
	}
	
	public Faction findGroupByName(String groupName){
		return this.dao.findGroupByName(groupName);
	}
	
	public FactionMember findMemberByGroup(String groupName, String memberName){
		return this.dao.findGroupMember(groupName, memberName);
	}
	
	public void addMemberToGroup(FactionMember factionMember){
		this.dao.save(factionMember);
	}
	
	public void removeMemberFromGroup(FactionMember factionMember){
		this.dao.delete(factionMember);
	}
	
	public void removeAllMembersFromGroup(String groupName){
		this.dao.removeAllMembersFromGroup(groupName);
	}
	
	public Set<Faction> findGroupsByFounder(String founderName){
		return this.dao.findGroupsByFounder(founderName);
	}
	
	public Set<Faction> findGroupsByMember(String memberName){
		Set<Faction> groups = new HashSet<Faction>();
		for(FactionMember factionMember : this.dao.findGroupsByMember(memberName)){
			Faction group = new Faction(factionMember.getFactionName(), factionMember.getMemberName());
			groups.add(group);
		}
		return groups;
	}
	
	public Set<FactionMember> findMembersOfGroup(String groupName){
		return this.dao.findMembersOfGroup(groupName);
	}
	
	public boolean hasGroupMember(String groupName, String memberName){
		return this.dao.hasGroupMember(groupName, memberName);
	}

	public boolean isGroup(String groupName) {
		if(this.dao.findGroup(groupName) != null){
			return true;
		}
		return false;
	}
	
	public boolean hasGroupModerator(String groupName, String memberName){
		return this.dao.hasGroupModerator(groupName, memberName);
	}
	
	public void addModeratorToGroup(Moderator moderator){
		this.dao.save(moderator);
	}
	
	public void removeModeratorToGroup(Moderator moderator){
		this.dao.delete(moderator);
	}

	public Set<Faction> findGroupsByModerator(String memberName) {
		Set<Faction> groups = new HashSet<Faction>();
		for(Moderator mod : this.dao.findGroupsByModerator(memberName)){
			Faction group = new Faction(mod.getFactionName(), mod.getMemberName());
			groups.add(group);
		}
		return groups;
	}

	public Set<Moderator> findModeratorsOfGroup(String groupName) {
		return this.dao.findModeratorsOfGroup(groupName);
	}

	public void removeAllModeratorsFromGroup(String groupName) {
		this.dao.removeAllModeratorsFromGroup(groupName);
	}

	public int findGroupsAmount() {
		return this.dao.countGroups();
	}

	public int findPlayerGroupsAmount(String playerName) {
		return this.dao.countPlayerGroups(playerName);
	}
}
