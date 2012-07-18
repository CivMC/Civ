package com.untamedears.citadel;

import com.untamedears.citadel.dao.CitadelDao;
import com.untamedears.citadel.entity.Member;

/**
 * User: JonnyD
 * Date: 07/18/12
 * Time: 11:57 PM
 */
public class MemberStorage {

	private CitadelDao dao;
	
	public MemberStorage(CitadelDao dao){
		this.dao = dao;
	}

	public void addMember(Member member){
		this.dao.save(member);
	}

	public void removeMember(Member member) {
		this.dao.delete(member);
	}
	
	public Member getMember(String memberName){
		return this.dao.findMember(memberName);
	}
}
