package com.untamedears.citadel;

import com.untamedears.citadel.entity.PersonalGroup;

/**
 * User: JonnyD
 * Date: 07/18/12
 * Time: 11:57 PM
 */
public class PersonalGroupManager {

	private PersonalGroupStorage storage;
	
	public void setStorage(PersonalGroupStorage storage){
		this.storage = storage;
	}
	
	public PersonalGroupStorage getStorage(){
		return this.storage;
	}
	
	public PersonalGroup getPersonalGroup(String ownerName){
		return this.storage.findPersonalGroup(ownerName);
	}
	
	public void addPersonalGroup(String groupName, String ownerName){
		addPersonalGroup(new PersonalGroup(groupName, ownerName));
	}
	
	public void addPersonalGroup(PersonalGroup group){
		this.storage.addPersonalGroup(group);
	}
	
	public void removePersonalGroup(String groupName, String ownerName){
		removePersonalGroup(new PersonalGroup(groupName, ownerName));
	}
	
	public void removePersonalGroup(PersonalGroup group){
		this.storage.removePersonalGroup(group);
	}
	
	public boolean hasPersonalGroup(String memberName){
		if(getPersonalGroup(memberName) != null){
			return true;
		}
		return false;
	}
}
