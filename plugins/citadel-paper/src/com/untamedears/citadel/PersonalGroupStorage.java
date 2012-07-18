package com.untamedears.citadel;

import com.untamedears.citadel.dao.CitadelDao;
import com.untamedears.citadel.entity.PersonalGroup;

/**
 * User: JonnyD
 * Date: 07/18/12
 * Time: 11:57 PM
 */
public class PersonalGroupStorage {

	private CitadelDao dao;
	
	public PersonalGroupStorage(CitadelDao dao){
		this.dao = dao;
	}

	public void addPersonalGroup(PersonalGroup group) {
		this.dao.save(group);	
	}

	public void removePersonalGroup(PersonalGroup group) {
		this.dao.delete(group);
	}

	public PersonalGroup findPersonalGroup(String ownerName){
		return this.dao.findPersonalGroup(ownerName);
	}
}
