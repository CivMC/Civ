package com.untamedears.citadel.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.untamedears.citadel.Citadel;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/21/12
 * Time: 1:14 AM
 */
@Entity
public class Faction implements Serializable {

	private static final long serialVersionUID = -1660849671051487634L;
	
	@Id private String name;
    private String founder;
    private String password;

    public Faction() {}

    public Faction(String name, String founder) {
        this.name = name;
        this.founder = founder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFounder() {
        return founder;
    }

    public void setFounder(String founder) {
        this.founder = founder;
    }
    
    public String getPassword(){
    	return password;
    }
    
    public void setPassword(String password){
    	this.password = password;
    }
    
    public boolean isFounder(String memberName){
    	return isFounder(new Member(memberName));
    }
    
    public boolean isFounder(Member member){
    	if(member.getMemberName().equalsIgnoreCase(this.founder)){
    		return true;
    	}
    	return false;
    }

    public boolean isMember(String memberName) {
    	return Citadel.getGroupManager().hasGroupMember(this.name, memberName);
    }
    
    public boolean isModerator(String memberName){
    	return Citadel.getGroupManager().hasGroupModerator(this.name, memberName);
    }
    
    public boolean isPersonalGroup(){
    	PersonalGroup personalGroup = Citadel.getPersonalGroupManager().getPersonalGroup(this.founder);
    	if(personalGroup != null && personalGroup.getGroupName().equals(this.name)){
    		return true;
    	}
    	return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o == null) return false;
        if (!(o instanceof Faction)) return false;

        Faction faction = (Faction) o;
        return this.name.equalsIgnoreCase(faction.getName());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
