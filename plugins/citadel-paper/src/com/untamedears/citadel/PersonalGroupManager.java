package com.untamedears.citadel;

import java.util.UUID;

import com.untamedears.citadel.dao.CitadelDao;
import com.untamedears.citadel.entity.PersonalGroup;

/**
 * User: JonnyD
 * Date: 07/18/12
 * Time: 11:57 PM
 */
public class PersonalGroupManager {

	private CitadelDao dao_;

    public void initialize(CitadelDao dao) {
        dao_ = dao;
    }
	
	public PersonalGroup getPersonalGroup(UUID accountId){
		return this.dao_.findPersonalGroup(accountId.toString());
	}
	
	public void addPersonalGroup(String groupName, UUID ownerAccountId){
		addPersonalGroup(new PersonalGroup(groupName, ownerAccountId.toString()));
	}
	
	public void addPersonalGroup(PersonalGroup group){
		this.dao_.save(group);	
	}
	
	public void removePersonalGroup(String groupName, UUID ownerAccountId){
		removePersonalGroup(new PersonalGroup(groupName, ownerAccountId.toString()));
	}
	
	public void removePersonalGroup(PersonalGroup group){
		this.dao_.delete(group);
	}

	public boolean hasPersonalGroup(UUID accountId){
		return getPersonalGroup(accountId) != null;
	}
}
