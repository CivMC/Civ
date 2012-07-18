package com.untamedears.citadel.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.untamedears.citadel.Citadel;

/**
 * User: JonnyD
 * Date: 7/18/12
 * Time: 11:57 PM
 */
@Entity
public class Member implements Serializable {

	private static final long serialVersionUID = -219411751058578508L;
	
	@Id private String memberName;
	private Faction activeGroup;
    private Set<Faction> groups = new HashSet<Faction>();

    public Member() {}

    public Member(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        Member that = (Member) o;
        return memberName.equals(that.memberName);
    }

    @Override
    public int hashCode() {
        int result = 31 * memberName.hashCode();
        return result;
    }
    
    public Faction getActiveGroup(){
    	return this.activeGroup;
    }
    
    public void setActiveGroup(Faction activeGroup){
    	this.activeGroup = activeGroup;
    }
    
    public Faction getPersonalGroup(){
    	PersonalGroup personalGroup = Citadel.getPersonalGroupManager().getPersonalGroup(this.memberName);
    	Faction group = Citadel.getGroupManager().getGroup(personalGroup.getGroupName());
    	return group;
    }
    
    public boolean hasPersonalGroup(){
    	if(getPersonalGroup() != null){
    		return true;
    	}
    	return false;
    }
    
	public Set<Faction> getGroups() {
		return this.groups;
	}

	public void setGroups(Set<Faction> groups){
		this.groups = groups;
	}

	public boolean hasFaction(Faction faction) {
		return this.groups.contains(faction);
	}
}
