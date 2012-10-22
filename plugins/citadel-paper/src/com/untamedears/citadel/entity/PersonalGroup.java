package com.untamedears.citadel.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * User: JonnyD
 * Date: 7/18/12
 * Time: 11:57 PM
 */
@Entity(name="personal_group")
public class PersonalGroup implements Serializable {

	private static final long serialVersionUID = -5051617245015238200L;

	@Id private String groupName;
	private String ownerName;
	
	public PersonalGroup(){}
	
	public PersonalGroup(String groupName, String ownerName){
		this.groupName = groupName;
		this.ownerName = ownerName;
	}
	
	public String getGroupName(){
		return this.groupName;
	}
	
	public void setGroupName(String groupName){
		this.groupName = groupName;
	}
	
	public String getOwnerName(){
		return this.ownerName;
	}
	
	public void setOwnerName(String ownerName){
		this.ownerName = ownerName;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FactionMember)) return false;
        PersonalGroup that = (PersonalGroup) o;
        return groupName.equals(that.groupName);
    }

    @Override
    public int hashCode() {
        int result = groupName.hashCode();
        result = 31 * result;
        return result;
    }	
}
